package de.voteeasy.invitation;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.UserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@Validated
public class RegisterByUserTokenService {
    private static final ServiceResult ERROR = ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("token.redeemedAlready").build();
    private final UserTokenRepository userTokenRepository;
    private final UserRegistrationService userRegistrationService;

    @Autowired
    public RegisterByUserTokenService(UserTokenRepository userTokenRepository, UserRegistrationService userRegistrationService) {
        this.userTokenRepository = userTokenRepository;
        this.userRegistrationService = userRegistrationService;
    }

    private Optional<UserToken> getToken(String token) {
        return userTokenRepository.findByTokenIgnoreCaseAndOrganisationEnabledIsTrue(token);
    }

    public ServiceResult redeemToken(@Valid @NotNull RegisterByUserTokenForm form) {
        // 1.check if token exists with trim to avoid copy&paste errors
        String token = form.getToken();
        if (StringUtils.isEmpty(token)) {
            log.info("Unable to redeem empty token.");
            return ERROR;
        }

        // 2.and only allow redemption if tenant is enabled and token exists
        Optional<UserToken> userToken = getToken(token.trim());
        if (userToken.isEmpty()) {
            log.info("Token '{}' cannot be redeemed as tenant is disabled or token itself unknown.", token);
            return ERROR;
        }

        return userRegistrationService.createUserFromToken(form, userToken.get().getOrganisation());
    }
}
