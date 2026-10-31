package de.voteeasy.participation;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.UUID;

public interface ParticipationRepository extends JpaRepository<Participation, UUID> {

    // all open participations for a given user in case of problems during logout/deployment
    Set<Participation> findByOwnerAndStoppedAtIsNull(User owner);

    // all active participants of a gathering
    @Query("SELECT new de.voteeasy.participation.Participant(g.name, p.startedAt, p.owner.userName, p.owner.fullName) " +
            "FROM Participation p, Gathering g " +
            "WHERE p.stoppedAt IS NULL " +
            "AND g = :gathering " +
            "AND p.event = :gathering " +
            "AND g.organisation = :owner " +
            "ORDER BY p.startedAt")
    Set<Participant> findAllByEventAndOwnerAndStoppedAtIsNull(Gathering gathering, Tenant owner);

    // set of all for a given user
    Set<Participation> findByOwnerOrderByStartedAtDesc(User owner);

    // remove all by event (used for cascading deletes
    Long deleteAllByEvent(Gathering gathering);

    long countAllByEvent_Organisation(Tenant tenant);
}
