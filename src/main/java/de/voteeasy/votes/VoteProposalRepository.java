package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface VoteProposalRepository extends JpaRepository<VoteProposal, UUID> {
    Set<VoteProposal> findByName(String name);

    Set<VoteProposal> findByGatheringAndStartedAtNotNullAndStoppedAtNotNullOrderByStoppedAt(Gathering gathering);

    long countAllByGathering_Organisation(Tenant tenant);
}
