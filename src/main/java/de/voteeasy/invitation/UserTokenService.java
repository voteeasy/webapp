package de.voteeasy.invitation;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.tenant.ReadSettingService;
import de.voteeasy.settings.tenant.SettingScope;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Transactional
@Validated
public class UserTokenService {
    private static final int DEFAULT_MAX_TOKEN_PER_TENANT = 50;
    private static final String SETTING_KEY_MAX_TOKEN_PER_TENANT = "usertoken.count.max";
    private static final int TOKEN_LENGTH = 11;
    private final UserTokenRepository userTokenRepository;
    private final ApplicationUserService applicationUserService;
    private final ReadSettingService readSettingService;
    private final TenantService tenantService;

    @Autowired
    public UserTokenService(UserTokenRepository userTokenRepository, ApplicationUserService applicationUserService, ReadSettingService readSettingService, TenantService tenantService) {
        this.userTokenRepository = userTokenRepository;
        this.applicationUserService = applicationUserService;
        this.readSettingService = readSettingService;
        this.tenantService = tenantService;
    }

    public Set<UserToken> getToken() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            return Collections.emptySet();
        }
        return getToken(user.get().getOrganisation());
    }

    public Set<UserToken> getUsableToken() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            return Collections.emptySet();
        }
        return userTokenRepository.findAllByEnabledIsTrueAndRedeemedAtIsNullAndOrganisation(user.get().getOrganisation());
    }

    public Set<UserToken> getToken(Tenant tenant) {
        if (tenant == null) {
            return Collections.emptySet();
        }
        return userTokenRepository.findAllByOrganisation(tenant, Sort.by(Sort.Direction.ASC, "token"));
    }

    public ServiceResult createToken() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        int currentTokenNumber = getToken().size();
        int tenantMaxTokenCount = readSettingService.getIntegerOrDefault(SETTING_KEY_MAX_TOKEN_PER_TENANT, DEFAULT_MAX_TOKEN_PER_TENANT, SettingScope.TENANT);

        if (currentTokenNumber >= tenantMaxTokenCount) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("token.limit.reached").build();
        }

        if(tenantService.isProtected(user.get().getOrganisation())) {
            return ServiceResult.builder().status(HttpStatus.METHOD_NOT_ALLOWED).messageKey("error.tenant.protected").build();
        }

        IntStream.range(currentTokenNumber, tenantMaxTokenCount).forEach(c -> generateToken(user.get().getOrganisation()));

        log.info("Generated {} tokens for tenant {}", tenantMaxTokenCount - currentTokenNumber, user.get().getOrganisation().getId());
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("token.generated").build();
    }

    private void generateToken(@NotNull Tenant tenant) {
        // generate a new token until no errors happen
        while (true) {
            try {
                UserToken token = UserToken.builder()
                        .organisation(tenant)
                        .token(String.valueOf(UUID.randomUUID()).substring(0, TOKEN_LENGTH).toUpperCase())
                        .build();
                userTokenRepository.save(token);
                return;
            } catch (Exception e) {
                log.info("Generating a new token as an exception happened, probably unique constraint violation.", e);
            }
        }
    }

    public ServiceResult toggleToken(UUID tokenId) {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty() || tokenId == null) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        final Optional<UserToken> token = userTokenRepository.findByIdAndOrganisation(tokenId, user.get().getOrganisation());
        if (token.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        UserToken tokenToToggle = token.get();
        tokenToToggle.setEnabled(!tokenToToggle.isEnabled());
        userTokenRepository.save(tokenToToggle);

        return ServiceResult.builder().status(HttpStatus.OK).messageKey("token.toggled").build();
    }

    public ServiceResult deleteToken(UUID tokenId) {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty() || tokenId == null) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        final Optional<UserToken> token = userTokenRepository.findByIdAndOrganisation(tokenId, user.get().getOrganisation());
        if (token.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        userTokenRepository.deleteById(tokenId);
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("token.deleted").build();
    }

    public ServiceResult redeemToken(UUID tokenId, Long timestamp) {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty() || tokenId == null) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }
        final Optional<UserToken> token = userTokenRepository.findByIdAndOrganisation(tokenId, user.get().getOrganisation());
        if (token.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        UserToken t = token.get();
        if (t.getRedeemedAt() != null) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("token.alreadyredeemed").build();
        }
        t.setRedeemedAt(timestamp);
        userTokenRepository.save(t);

        return ServiceResult.builder().status(HttpStatus.OK).messageKey("token.redeemed").build();
    }

    long getRedeemedTokenCount() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        return user.map(value -> userTokenRepository.countByRedeemedAtIsNotNullAndOrganisation(value.getOrganisation())).orElse(0L);
    }

    long getOpenTokenCount() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        return user.map(value -> userTokenRepository.countByRedeemedAtIsNullAndOrganisation(value.getOrganisation())).orElse(0L);
    }

    long getDisabledTokenCount() {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        return user.map(value -> userTokenRepository.countByEnabledIsFalseAndOrganisation(value.getOrganisation())).orElse(0L);
    }
}
