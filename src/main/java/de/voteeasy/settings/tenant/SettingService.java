package de.voteeasy.settings.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.voteeasy.settings.tenant.SettingScope.TENANT;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@Transactional
public class SettingService {
    private final ApplicationUserService applicationUserService;
    private final SettingRepository settingRepository;
    private final TenantService tenantService;

    @Autowired
    public SettingService(ApplicationUserService applicationUserService, SettingRepository settingRepository, TenantService tenantService) {
        this.applicationUserService = applicationUserService;
        this.settingRepository = settingRepository;
        this.tenantService = tenantService;
    }

    public Optional<Setting> getTenantSetting(String key, Tenant targetTenant) {
        return getSetting(key, TENANT, targetTenant);
    }

    public Optional<Setting> getSetting(String key, SettingScope scope, Tenant targetTenant) {
        if (key == null) {
            return Optional.empty();
        }

        final Set<Setting> settings = settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(key.trim(), scope == null ? TENANT : scope, targetTenant);
        if (settings.isEmpty()) {
            return Optional.empty();
        }

        if (settings.size() > 1) {
            log.error("INTERNAL ERROR: more than one setting in database for given key {} and scope {}. This might be a problem. Will only return first entry!", key, scope);
        }

        return settings.stream().findFirst();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Setting> getMySettings() {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("ADMIN");
        if (user.isPresent()) {
            return getSettings(user.get().getOrganisation());
        }

        return Collections.emptySortedSet();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    private Set<Setting> getSettings(Tenant t) {
        return settingRepository.getAllByTenant(t);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Setting> getSettings(UUID id) {
        Optional<Tenant> t = tenantService.getTenant(id);
        return t.isPresent() ? getSettings(t.get()) : Collections.emptySortedSet();
    }

    public Set<SettingKeyUsage> getAllKeys(SettingScope scope) {
        return settingRepository.getAllByScope(scope);
    }

    public ServiceResult addProperty(@Valid SettingPropertyForm form, Tenant tenant) {
        Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN");
        if (form.getUserId() == null || user.isEmpty() || !form.getUserId().equals(user.get().getId()) || tenant == null) {
            return ServiceResult.builder().status(HttpStatus.METHOD_NOT_ALLOWED).messageKey("error.unknownId").build();
        }

        if (tenantService.isProtected(tenant)) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenant.protected").build();
        }

        if (getSetting(form.getKey(), SettingScope.valueOf(form.getScope()), tenant).isPresent()) {
            return ServiceResult.builder().status(HttpStatus.CONFLICT).messageKey("error.propertyexists").build();
        }

        User u = user.get();
        Setting setting = SettingPropertyFormMapper.toEntity(form);
        setting.setCreatedBy(u);
        setting.setTenant(tenant);
        settingRepository.save(setting);
        return ServiceResult.builder().messageKey("property.create.success").status(HttpStatus.OK).build();
    }

    public Optional<Tenant> getTenantDetails(UUID id) {
        return tenantService.getTenant(id);
    }

    public ServiceResult deleteProperty(UUID id) {
        if (id == null) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        settingRepository.deleteById(id);
        return ServiceResult.builder().messageKey("property.delete.success").status(HttpStatus.OK).build();
    }
}
