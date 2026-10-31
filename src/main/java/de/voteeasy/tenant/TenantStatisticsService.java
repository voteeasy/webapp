package de.voteeasy.tenant;

import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@Transactional(readOnly = true)
class TenantStatisticsService {
    private final UserTokenRepository userTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    TenantStatisticsService(UserTokenRepository userTokenRepository, UserRepository userRepository) {
        this.userTokenRepository = userTokenRepository;
        this.userRepository = userRepository;
    }

    long getUserTokenCount(Tenant t) {
        return userTokenRepository.countAllByOrganisation(t);
    }

    long getActiveUserCount(Tenant t) {
        return userRepository.countAllByOrganisation(t);
    }

    long getAvailableUserTokenCount(Tenant t) {
        return userTokenRepository.countByRedeemedAtIsNullAndOrganisation(t);
    }

    long getDisabledUserTokenCount(Tenant t) {
        return userTokenRepository.countByEnabledIsFalseAndOrganisation(t);
    }

    long getAlreadyRedeemedUserTokenCount(Tenant t) {
        return userTokenRepository.countByRedeemedAtIsNotNullAndOrganisation(t);
    }
}
