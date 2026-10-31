package de.voteeasy.seeding;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.Role;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSeedDataCreatorTest {
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    @ExpectSystemExitWithStatus(-1)
    void bootstrapUserRelatedDataWillExitIfDataIsNotFound() {
        new UserSeedDataCreator().bootstrapUserRelatedData(roleRepository, userRepository, tenantRepository);
    }

    private void prepareMocks(boolean withRoles) {
        if (withRoles) {
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role()));
            when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new Role()));
            when(roleRepository.findByName("ROLE_TENANT_ADMIN")).thenReturn(Optional.of(new Role()));
        }

        for (String tenant : UserSeedDataCreator.TENANTS) {
            Tenant t = new Tenant(tenant);
            t.setId(UUID.randomUUID());

            when(tenantRepository.findByName(eq(tenant))).thenReturn(Optional.of(t));
        }
    }

    @Test
    @ExpectSystemExitWithStatus(-1)
    void bootstrapUserRelatedDataWithoutRolesExisting() {
        prepareMocks(false);
        when(roleRepository.findByName(eq("ROLE_USER"))).thenReturn(Optional.empty());
        new UserSeedDataCreator().bootstrapUserRelatedData(roleRepository, userRepository, tenantRepository);
    }

    @Test
    void bootstrapUserRelatedData() {
        prepareMocks(true);
        new UserSeedDataCreator().bootstrapUserRelatedData(roleRepository, userRepository, tenantRepository);
        verify(userRepository, times(9)).save(any());
    }

    @Test
    void bootstrapUserRelatedDataUserAlreadyExists() {
        prepareMocks(true);
        when(userRepository.getByUserInTenant(eq("gongbing"), any(Tenant.class))).thenReturn(Optional.of(new User()));
        new UserSeedDataCreator().bootstrapUserRelatedData(roleRepository, userRepository, tenantRepository);
        verify(userRepository, times(0)).save(any());
    }

}