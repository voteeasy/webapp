package de.voteeasy.votes;

import de.voteeasy.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Entity
@Getter
@Setter
public class Vote implements Serializable, Comparable<Vote> {
    @Serial
    private static final long serialVersionUID = 8579305787951149198L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private Long createdAt = System.currentTimeMillis();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Option> chosenOptions = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    private User voter;

    @OneToOne(fetch = FetchType.LAZY)
    private VoteProposal proposal;

    public Vote() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return id.equals(vote.id) && createdAt.equals(vote.createdAt) && Objects.equals(voter, vote.voter) && Objects.equals(proposal, vote.proposal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, voter, proposal);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vote{");
        sb.append("id=").append(id);
        sb.append(", createdAt=").append(createdAt);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Vote o) {
        return Comparator.comparing(Vote::getCreatedAt).compare(this, o);
    }
}
