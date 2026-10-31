package de.voteeasy.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationUserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private ApplicationUser springUser;
    @InjectMocks
    private ApplicationUserService applicationUserService;

    @Test
    void wiringWorks() {
        assertThat(applicationUserService).isNotNull();
    }

    @Test
    void extractUserIsEmptyWithoutPreconditions() {
        assertThat(applicationUserService.currentlyLoggedInUser()).isEmpty();
    }

    @Test
    void extractUserWorksIfAuthenticationDoesNotReturnPrincipal() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertThat(applicationUserService.currentlyLoggedInUser()).isEmpty();
    }

    @Test
    void extractUserWorksIfUserNotFoundInDatabaseButLoggedIn() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(springUser);
        when(springUser.getUsername()).thenReturn("AUserLoggedInButInNoDatabase");
        when(springUser.getTenant()).thenReturn(UserTenant.builder().tenantName("").build());

        assertThat(applicationUserService.currentlyLoggedInUser()).isEmpty();
    }

    @Test
    void extractUserWorksIfConfiguredInSpringAndInApplicationDatabase() {
        User databaseUser = haveAUserLoggedIn();
        assertThat(applicationUserService.currentlyLoggedInUser()).isPresent().contains(databaseUser);
    }

    private User haveAUserLoggedIn(String... roles) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(springUser);
        when(springUser.getUsername()).thenReturn("AUserLoggedInCanBeFoundInTheDatabase");
        when(springUser.getTenant()).thenReturn(UserTenant.builder().tenantName("").build());

        User databaseUser;
        if (roles == null || roles.length == 0) {
            databaseUser = User.builder().userName("AUserLoggedInCanBeFoundInTheDatabase").id(UUID.randomUUID()).build();
        } else {
            databaseUser = User.builder().userName("AUserLoggedInCanBeFoundInTheDatabase").roles(Arrays.stream(roles).map(Role::new).collect(Collectors.toList())).id(UUID.randomUUID()).build();
        }

        when(userRepository.getForLogin(eq("AUserLoggedInCanBeFoundInTheDatabase"), any())).thenReturn(Optional.of(databaseUser));
        return databaseUser;
    }

    @Test
    void currentlyLoggedInUserHasAtLeastOneOfRolesNoRolesGiven() {
        haveAUserLoggedIn();
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles((String[])null)).isEmpty();
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles()).isEmpty();
    }

    @Test
    void currentlyLoggedInUserHasAtLeastOneOfRolesNoUserPresent() {
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("roleButNoUser")).isEmpty();
    }

    @Test
    void currentlyLoggedInUserHasAtLeastOneOfRolesWithOneRoleAndImplicitPrefixSet() {
        assertThat(haveAUserLoggedIn("role_aha")).isNotNull();
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("aha")).isPresent();
    }

    @Test
    void currentlyLoggedInUserHasAtLeastOneOfRolesWithMoreRolesAndImplicitPrefixSet() {
        assertThat(haveAUserLoggedIn("role_none", "none", "role_aha")).isNotNull();
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("aha")).isPresent();
    }

    @Test
    void currentlyLoggedInUserHasAtLeastOneOfRolesWithoutRoleHit() {
        assertThat(haveAUserLoggedIn("none", "foo", "bar")).isNotNull();
        assertThat(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("aha")).isEmpty();
    }
}