package de.voteeasy.seeding;

import de.voteeasy.user.Role;
import de.voteeasy.user.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileSeedDataCreatorTest {
    private static final int EXPECTED_ROLE_COUNT = 3;

    @Mock
    private RoleRepository roleRepository;

    @Test
    void bootstrapRoleDataWithoutPersistenceMocks() {
        when(roleRepository.count()).thenReturn(4711L);
        new ProfileSeedDataCreator().bootstrapRoleData(roleRepository);
    }

    @Test
    void bootstrapRoleDataWithoutAnyDataExistingToCreateAllRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        new ProfileSeedDataCreator().bootstrapRoleData(roleRepository);
        verify(roleRepository, times(EXPECTED_ROLE_COUNT)).save(any());
    }

    @Test
    void bootstrapRoleDataWitPartialDataExistence() {
        lenient().when(roleRepository.findByName(eq("ROLE_ADMIN"))).thenReturn(Optional.of(new Role()));

        new ProfileSeedDataCreator().bootstrapRoleData(roleRepository);
        verify(roleRepository, times(EXPECTED_ROLE_COUNT - 1)).save(any());
    }

}