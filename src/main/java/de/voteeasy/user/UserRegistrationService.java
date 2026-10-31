package de.voteeasy.user;

import de.voteeasy.invitation.RegisterByUserTokenForm;
import de.voteeasy.invitation.UserToken;
import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@Validated
public class UserRegistrationService {
    public static final String KEY_USERNAME = "tkn.username";
    public static final String KEY_TENANT = "tkn.tenant";
    private static final ServiceResult ERROR = ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("token.redeemedAlready").build();

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserRegistrationService(UserRepository userRepository, UserTokenRepository userTokenRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.roleRepository = roleRepository;
    }

    private static String trimNullSafe(String input) {
        if (input != null) {
            return input.trim();
        }
        return null;
    }

    private Role userRole() {
        // if that NPE happens something else seriously went wrong
        return roleRepository.findByName(RoleService.ROLE_PREFIX + "USER").get();
    }

    public ServiceResult createUserFromToken(@Valid @NotNull RegisterByUserTokenForm form, @NotNull Tenant tenant) {
        String userName = trimNullSafe(form.getToken());
        if (StringUtils.isEmpty(userName)) {
            return ERROR;
        }

        if (tenant == null) {
            log.error("Given tenant for redemption was not set for token '{}', but should have been.", userName);
            return ERROR;
        }

        if (isUserNameUsedAlready(userName)) {
            log.warn("User with name '{}' does already exist in tenant {} - no redemption possible.", userName, tenant.getId());
            return ERROR;
        }

        Optional<UserToken> redeemedToken = redeemToken(userName);
        if (redeemedToken.isEmpty()) {
            log.warn("Someone tried to redeem an unknown token '{}' in tenant '{}' - no redemption possible.", userName, tenant.getId());
            return ERROR;
        }

        User inDb = createUserInDatabase(form.getPassword(), userName, tenant);

        ServiceResult result = ServiceResult.builder().status(HttpStatus.OK).messageKey("token.redeemed").build();
        result.add(KEY_USERNAME, inDb.getUserName());
        result.add(KEY_TENANT, inDb.getOrganisation().getName());

        log.info("Registration with token '{}' was successful.", userName);
        return result;
    }

    private Optional<UserToken> redeemToken(String token) {
        // 4.set token to redeemed and save
        Optional<UserToken> t = userTokenRepository.findByTokenIgnoreCase(token);
        if (t.isEmpty()) {
            log.error("Unable to set redemption date on given token '{}'", token);
            return Optional.empty();
        }

        UserToken tokenDatabase = t.get();
        tokenDatabase.setRedeemedAt(System.currentTimeMillis());
        return Optional.of(userTokenRepository.save(tokenDatabase));
    }

    private User createUserInDatabase(String password, String userName, Tenant tenant) {
        // 3.create user with username == token/uppercased and fullName n/a in token's tenant
        User userByToken = User.builder()
                .userName(userName.toUpperCase())
                .fullName("n/a")
                .organisation(tenant)
                .passwordHash(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password.trim()))
                .roles(List.of(userRole()))
                .build();
        return userRepository.save(userByToken);
    }

    private boolean isUserNameUsedAlready(String userName) {
        return userRepository.findByUserNameIgnoreCase(userName).isPresent();
    }

}
