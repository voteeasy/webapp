package de.voteeasy.admin;

import de.voteeasy.AbstractIT;
import de.voteeasy.seeding.UserSeedDataCreator;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
public class AdminServiceITests extends AbstractIT {
    @Autowired
    private AdminService adminService;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void serviceIsWiredInCorrectly() {
        assertThat(tenantRepository).isNotNull();
        assertThat(adminService).isNotNull();
    }

    @Test
    void countsForUnknownTenantId() {
        CardinalityResult result = adminService.calculateCardinalities(UUID.randomUUID());
        assertThat(result).isNotNull();
    }

    @Test
    void verifyNativeQueriesForAllTenants() {
        CardinalityResult result = adminService.calculateCardinalities();
        assertThat(result).isNotNull();
        assertThat(result.getTableCardinalities()).hasSize(11); // 10 data tables plus ROLE

        // all entries need to be zero during tests as no bootstrapped data exists
        for (Map.Entry<String, Long> entry : result.getTableCardinalities().entrySet()) {
            assertThat(entry.getValue()).isEqualTo(0L);
        }
    }

    @Test
    void verifyNativeQueriesWithSpecificTenant() {
        // create example tenant
        Tenant t = new Tenant(UserSeedDataCreator.VOTE_EASY);
        tenantRepository.save(t);

        Optional<Tenant> tenant = tenantRepository.findByName(UserSeedDataCreator.VOTE_EASY);
        assertThat(tenant).isPresent();

        CardinalityResult result = adminService.calculateCardinalities(tenant.get().getId());
        assertThat(result).isNotNull();
        assertThat(result.getTableCardinalities()).hasSize(10);
        assertThat(result.getTableCardinalities().get("Tenant")).isEqualTo(1L);
        assertThat(result.getTableCardinalities().get("PersistentLogins")).isEqualTo(0L);
    }

}
