package de.voteeasy.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationUserService {
    private final UserRepository userRepository;

    @Autowired
    public ApplicationUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> currentlyLoggedInUserHasAtLeastOneOfRoles(String... roles) {
        Optional<User> user = currentlyLoggedInUser();
        if (user.isPresent() && roles != null) {
            Collection<Role> userRoles = user.get().getRoles();
            if (userRoles != null && !userRoles.isEmpty()) {
                for (Role r : userRoles) {
                    for (String aRole : roles) {
                        if (r.getName().equalsIgnoreCase(RoleService.ROLE_PREFIX + aRole)) {
                            return user;
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Optional<User> currentlyLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("No user is currently logged in - expected to happen during application startup, but must not happen if application is up'n'running.");
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof ApplicationUser u) {
            Optional<User> userFromDatabase = userRepository.getForLogin(u.getUsername(), u.getTenant().getTenantName());
            if (userFromDatabase.isEmpty()) {
                log.error("Serious problem, currently logged in user could not be retrieved from database. You have been hacked by {}!", u);
            } else {
                return userFromDatabase;
            }
        }

        return Optional.empty();
    }
}
