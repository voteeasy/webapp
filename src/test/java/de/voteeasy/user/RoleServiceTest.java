package de.voteeasy.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getByNameWithoutPrefix() {
        String role = "getByNameWithoutPrefix";
        Role r = new Role(role);
        when(roleRepository.findByName(eq("ROLE_" + role))).thenReturn(Optional.of(r));

        final Optional<Role> roleFromDatabase = roleService.getByNameWithoutPrefix(role);
        assertThat(roleFromDatabase).isPresent().hasValue(r);
    }

    @ParameterizedTest(name = "#{index} with value \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", " empty ", "notFoundInDatabase"})
    void getByNameWithoutPrefixWithEmptyNames(String name) {
        assertThat(roleService.getByNameWithoutPrefix(name)).isEmpty();
    }


    @Test
    void wiringWorks() {
        assertThat(roleService).isNotNull();
    }


}