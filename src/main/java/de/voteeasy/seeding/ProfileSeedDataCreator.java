package de.voteeasy.seeding;

import de.voteeasy.user.Role;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.RoleService;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProfileSeedDataCreator {
    private static final List<String> ROLES = List.of("USER", "ADMIN", "TENANT_ADMIN");

    private static void createIfNotExistingWithCorrectPrefix(String roleName, RoleRepository roleRepository) {
        Optional<Role> role = roleRepository.findByName(RoleService.ROLE_PREFIX + roleName);
        if (role.isPresent()) {
            log.info("Not recreating already existing role with name {}", roleName);
            return;
        }

        roleRepository.save(new Role(RoleService.ROLE_PREFIX + roleName));

        log.info("Created role {}", roleName);
    }

    public void bootstrapRoleData(@NotNull RoleRepository roleRepository) {
        log.info("Bootstrapping ROLE data ....");

        for (String roleName : ROLES) {
            createIfNotExistingWithCorrectPrefix(roleName, roleRepository);
        }
        log.info("ROLE - Bootstrapping finished with {} roles", roleRepository.count());
    }

}
