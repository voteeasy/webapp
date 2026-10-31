package de.voteeasy.votes;

import de.voteeasy.tenant.Tenant;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {
    Set<Option> findByNameInAndVoteProposalEquals(Set<String> names, VoteProposal voteProposal, Sort sort);

    long countAllByVoteProposal_Gathering_Organisation(Tenant tenant);
}
