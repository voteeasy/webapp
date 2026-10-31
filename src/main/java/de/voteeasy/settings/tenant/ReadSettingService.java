package de.voteeasy.settings.tenant;

import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ReadSettingService {
    private final ApplicationUserService applicationUserService;
    private final SettingRepository settingRepository;
    private final TenantService tenantService;

    @Autowired
    public ReadSettingService(ApplicationUserService applicationUserService, SettingRepository settingRepository, TenantService tenantService) {
        this.applicationUserService = applicationUserService;
        this.settingRepository = settingRepository;
        this.tenantService = tenantService;
    }

    public int getIntegerOrDefault(String key, int fallbackDefault, SettingScope scope) {

        Optional<User> u = applicationUserService.currentlyLoggedInUser();
        if (u.isEmpty()) {
            return fallbackDefault;
        }

        User currentUser = u.get();
        if (tenantService.isProtected(currentUser.getOrganisation())) {
            return fallbackDefault;
        }

        return parsedValueOrDefault(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(key, scope == null ? SettingScope.TENANT : scope, currentUser.getOrganisation()), fallbackDefault);
    }

    private int parsedValueOrDefault(@NonNull Set<Setting> settingsFromDatabase, int fallbackDefault) {
        final Optional<Setting> setting = settingsFromDatabase.stream().findFirst();
        if (setting.isPresent()) {
            Setting s = setting.get();

            if (s.getType() == SettingType.NUMERIC) {
                try {
                    return Integer.parseInt(s.getValue());
                } catch (NumberFormatException e) {
                    log.info("Unable to parse value {}, will return default {}", s.getValue(), fallbackDefault);
                    // return fallback
                }
            }
        }
        return fallbackDefault;
    }
}
