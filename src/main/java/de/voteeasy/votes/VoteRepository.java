package de.voteeasy.votes;

import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Set<Vote> findByVoter(User user);

    Set<Vote> findByProposal(VoteProposal voteProposal);

    Optional<Vote> findByVoterAndProposal(User user, VoteProposal voteProposal);

    Long deleteAllByProposal(VoteProposal voteProposal);

    long countAllByVoter_Organisation(Tenant tenant);
}
