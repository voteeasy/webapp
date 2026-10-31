package de.voteeasy.invitation;

import de.voteeasy.tenant.Tenant;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    // COUNT: all not-already redeemed token including disabled tokens
    long countByRedeemedAtIsNullAndOrganisation(Tenant organisation);

    // COUNT: all disabled tokens
    long countByEnabledIsFalseAndOrganisation(Tenant organisation);

    // COUNT: all already redeemed token
    long countByRedeemedAtIsNotNullAndOrganisation(Tenant organisation);

    long countAllByOrganisation(Tenant organisation);

    // all existing tokens
    Set<UserToken> findAllByOrganisation(Tenant organisation, Sort sort);

    // all usable tokens
    Set<UserToken> findAllByEnabledIsTrueAndRedeemedAtIsNullAndOrganisation(Tenant organisation);

    Optional<UserToken> findByIdAndOrganisation(UUID tenantId, Tenant organisation);

    Optional<UserToken> findByTokenIgnoreCaseAndOrganisationEnabledIsTrue(String token);

    Optional<UserToken> findByTokenIgnoreCase(String token);
}
