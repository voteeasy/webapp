package de.voteeasy.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByName(String name);

    Set<Tenant> findByNameIgnoreCase(String name);

    /**
     * Retrieve all tenants with creation date before given createdAt timestamp.
     * @param createdAt timestamp in milliseconds.
     * @return all tenants with creation date before the given date timestamp.
     */
    Set<Tenant> findByCreatedAtIsBeforeOrderByCreatedAtAsc(Long createdAt);
}
