package de.voteeasy.user;

import de.voteeasy.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u, Tenant t WHERE lower(u.userName) = lower(:userName) AND u.organisation = t AND lower(t.name) = lower(:tenantName)")
    Optional<User> getForLogin(String userName, String tenantName);

    @Query("SELECT u FROM User u, Tenant t WHERE u.userName = :userName AND u.organisation = :tenant")
    Optional<User> getByUserInTenant(String userName, Tenant tenant);

    @Query(value = "SELECT count(*) FROM public.PERSISTENT_LOGINS", nativeQuery = true)
    Long countPersistentLogins();

    @Query(value = "SELECT COUNT(pl.*) FROM public.PERSISTENT_LOGINS pl , public.User u WHERE u.user_name = pl.username AND u.tenant_name=?1", nativeQuery = true)
    long countPersistentLoginsByTenant(UUID tenantId);

    Long deleteAllByOrganisation(Tenant tenant);

    Optional<User> findByUserNameIgnoreCase(String userName);

    long countAllByOrganisation(Tenant tenant);
}
