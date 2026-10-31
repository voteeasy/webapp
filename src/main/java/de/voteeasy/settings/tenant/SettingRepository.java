package de.voteeasy.settings.tenant;

import de.voteeasy.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.UUID;

public interface SettingRepository extends JpaRepository<Setting, UUID> {
    Long deleteAllByTenant(Tenant tenant);

    Set<Setting> getAllByTenant(Tenant tenant);

    long countAllByTenant(Tenant tenant);

    Set<Setting> findAllByKeyIgnoreCaseAndScopeAndTenant(String key, SettingScope scope, Tenant tenant);

    @Query("SELECT new de.voteeasy.settings.tenant.SettingKeyUsage(s.key, COUNT(s.key), s.scope, s.type) FROM Setting s WHERE s.scope = :scope GROUP BY s.key, s.scope, s.type ORDER BY s.key")
    Set<SettingKeyUsage> getAllByScope(SettingScope scope);
}
