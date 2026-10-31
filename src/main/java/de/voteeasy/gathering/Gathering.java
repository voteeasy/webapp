package de.voteeasy.gathering;

import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.User;
import de.voteeasy.votes.VoteProposal;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Entity
public class Gathering implements Serializable, Comparable<Gathering> {
    @Serial
    private static final long serialVersionUID = -939190441553569371L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    private User startedBy;

    @Column
    private Long startedAt = System.currentTimeMillis();

    @Column(length = 256)
    @Size(max = 256)
    @NotEmpty(message = "{NotEmpty.gathering.name}")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "gathering")
    @OrderBy("itemOfAgenda, name, createdAt")
    private Set<VoteProposal> voteProposals = new TreeSet<>();

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    private Tenant organisation;

    public Gathering() {
    }

    public Gathering(String name, Tenant organisation) {
        this.name = name;
        this.organisation = organisation;
    }

    public Gathering(String name, Tenant organisation, User startedBy) {
        this.name = name;
        this.organisation = organisation;
        this.startedBy = startedBy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Gathering{");
        sb.append("id=").append(id);
        sb.append(", startedBy=").append(startedBy);
        sb.append(", startedAt=").append(startedAt);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gathering gathering = (Gathering) o;
        return id.equals(gathering.id) && Objects.equals(startedBy, gathering.startedBy) && startedAt.equals(gathering.startedAt) && name.equals(gathering.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startedBy, startedAt, name);
    }

    @Override
    public int compareTo(Gathering o) {
        return Comparator.comparing(Gathering::getName, Comparator.naturalOrder()).thenComparing(Gathering::getStartedAt).compare(this, o);
    }
}
