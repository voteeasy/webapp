package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Builder
@Getter
@Setter
@Entity
public class VoteProposal implements Serializable, Comparable<VoteProposal> {
    @Serial
    private static final long serialVersionUID = -7627421822709841576L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // German: TOP/Tagesordnungspunkt
    @Column(length = 100)
    private String itemOfAgenda;

    @Column(length = 100)
    private String name;

    @Column
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    // after the admin hit the vote now button this field is set
    @Column
    private Long startedAt;
    // after the admin hit the stop voting button this field is set
    @Column
    private Long stoppedAt;

    // should UserIds be reported
    @Builder.Default
    @Column
    private boolean isAnonymized = false;

    @Builder.Default
    @Column
    private boolean isAdminAllowedToVote = true;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "voteProposal")
    @Builder.Default
    @OrderBy("name")
    private Set<Option> options = new TreeSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Gathering gathering;

    public VoteProposal() {
    }

    public VoteProposal(UUID id, String itemOfAgenda, String name, Long createdAt, Long startedAt, Long stoppedAt, Boolean isAnonymized, Boolean isAdminAllowedToVote, Set<Option> options, Gathering gathering) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.stoppedAt = stoppedAt;
        this.isAnonymized = isAnonymized;
        this.isAdminAllowedToVote = isAdminAllowedToVote;
        this.options = options;
        this.gathering = gathering;
        this.itemOfAgenda = itemOfAgenda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoteProposal that = (VoteProposal) o;
        return isAdminAllowedToVote == that.isAdminAllowedToVote && isAnonymized == that.isAnonymized && id.equals(that.id) && name.equals(that.name) && createdAt.equals(that.createdAt) && Objects.equals(startedAt, that.startedAt) && Objects.equals(stoppedAt, that.stoppedAt) && Objects.equals(itemOfAgenda, that.itemOfAgenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt, startedAt, stoppedAt, isAnonymized, isAdminAllowedToVote, itemOfAgenda);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VoteProposal{");
        sb.append("id=").append(id);
        sb.append(", itemOfAgenda='").append(itemOfAgenda).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", startedAt=").append(startedAt);
        sb.append(", stoppedAt=").append(stoppedAt);
        sb.append(", isAnonymized=").append(isAnonymized);
        sb.append(", isAdminAllowedToVote=").append(isAdminAllowedToVote);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(VoteProposal other) {
        if (getItemOfAgenda() == null || other == null || other.getItemOfAgenda() == null) {
            return Comparator.comparing(VoteProposal::getName, Comparator.naturalOrder()).thenComparing(VoteProposal::getCreatedAt).compare(this, other);
        }
        return Comparator.comparing(VoteProposal::getItemOfAgenda, Comparator.naturalOrder()).thenComparing(VoteProposal::getName, Comparator.naturalOrder()).thenComparing(VoteProposal::getCreatedAt).compare(this, other);

    }
}
