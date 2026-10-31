package de.voteeasy.gathering;

import de.voteeasy.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GatheringRepository extends JpaRepository<Gathering, UUID> {
    Set<Gathering> findByOrganisation(Tenant tenant);
    long countAllByOrganisation(Tenant tenant);
    Optional<Gathering> findByIdAndOrganisation(UUID id, Tenant tenant);
}