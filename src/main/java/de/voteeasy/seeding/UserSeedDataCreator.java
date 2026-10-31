package de.voteeasy.seeding;

import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.NotNull;
import java.util.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class UserSeedDataCreator {
    public static final String VOTE_EASY = "VoteEasy";
    static final List<String> TENANTS = List.of("Gartenverein", "Partei", VOTE_EASY);

    private static Optional<Role> get(String roleName, RoleRepository roleRepository) {
        return roleRepository.findByName(RoleService.ROLE_PREFIX + roleName);
    }

    private static void prepareTenantData(TenantRepository tenantRepository) {
        log.info("Bootstrapping TENANT data ....");

        for (String name : TENANTS) {
            if (tenantRepository.findByName(name).isPresent()) {
                log.info("Tenant {} already exists, will skip tenant seeding for it.", name);
                continue;
            }

            if (name.equalsIgnoreCase("gartenverein")) {
                Tenant t = new Tenant(name);
                t.setEnabled(false);
                tenantRepository.save(t);
                log.warn("Tenant {} is disabled and no user will be allowed to login.", name);
            } else {
                tenantRepository.save(new Tenant(name));
            }
        }

        log.info("TENANT - Bootstrapping finished with {} tenants", tenantRepository.count());
    }

    public void bootstrapUserRelatedData(@NotNull RoleRepository roleRepository, @NotNull UserRepository userRepository, @NotNull TenantRepository tenantRepository) {
        log.info("Bootstrapping USER data ....");

        prepareTenantData(tenantRepository);

        for (String tenant : TENANTS) {
            Optional<Tenant> targetTenant = tenantRepository.findByName(tenant);
            if (targetTenant.isEmpty()) {
                log.error("Tenant {} not found - will shutdown immediately.", tenant);
                System.exit(-1);
            }

            Role user = null;
            Role admin = null;
            Role tenantAdmin = null;
            try {
                user = get("USER", roleRepository).get();
                admin = get("ADMIN", roleRepository).get();
                tenantAdmin = get("TENANT_ADMIN", roleRepository).get();
            } catch (NoSuchElementException e) {
                log.error("One of the expected roles does not exist. Will stop the application now.", e);
                System.exit(-1);
            }

            if (userRepository.getByUserInTenant("gongbing", targetTenant.get()).isEmpty()) {
                log.info("Bootstrapping example users");

                if (VOTE_EASY.equals(tenant)) {
                    log.warn("Current tenant has users with TENANT_ADMIN rights.");
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Gongbing").userName("gongbing").passwordHash("{bcrypt}$2a$10$YaeZWIX3MAgdV0i/O5VAD.SmOiQAPeQFD00iVwecEnwe7xZ98AiaG").roles(Arrays.asList(user, admin, tenantAdmin)).build());
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Emil").userName("emil").passwordHash("{bcrypt}$2a$10$pX5rXeUbru0eJHnVs25KuexoelApZ2jG4YIiap8atZ7bpgROacMNO").roles(Collections.singletonList(user)).build());
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Sandmann").userName("sandmann").passwordHash("{bcrypt}$2a$10$B7NvQNookfkGTDNYFb.TfuW.6NuyLhpQN9gCSVgN65GkXjKBRgOvO").roles(Arrays.asList(user, admin, tenantAdmin)).build());
                } else {
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Gongbing").userName("gongbing").passwordHash("{bcrypt}$2a$10$YaeZWIX3MAgdV0i/O5VAD.SmOiQAPeQFD00iVwecEnwe7xZ98AiaG").roles(Arrays.asList(user, admin)).build());
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Emil").userName("emil").passwordHash("{bcrypt}$2a$10$pX5rXeUbru0eJHnVs25KuexoelApZ2jG4YIiap8atZ7bpgROacMNO").roles(Collections.singletonList(user)).build());
                    userRepository.save(User.builder().organisation(targetTenant.get()).fullName("Sandmann").userName("sandmann").passwordHash("{bcrypt}$2a$10$B7NvQNookfkGTDNYFb.TfuW.6NuyLhpQN9gCSVgN65GkXjKBRgOvO").roles(Arrays.asList(user, admin)).build());
                }
            }

            for (String username : Arrays.asList("gongbing", "emil", "sandmann")) {
                log.info("Tenant {} is running with bootstrapped user {}", tenant, username);
            }
        }
        log.info("USER - Bootstrapping finished");
    }

}
