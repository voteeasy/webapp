package de.voteeasy.votes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Option implements Serializable, Comparable<Option> {
    @Serial
    private static final long serialVersionUID = -7735771759070543817L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 256)
    @NotEmpty(message = "{NotEmpty.option.name}")
    private String name;

    // do not use CascadeType.ALL as this causes detached objects saved errors
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_proposal_id")
    private VoteProposal voteProposal;

    public Option() {

    }

    public Option(@NotEmpty(message = "{NotEmpty.option.name}") String name) {
        this.name = name;
    }

    public Option(@NotEmpty(message = "{NotEmpty.option.name}") String name, VoteProposal voteProposal) {
        this.name = name;
        this.voteProposal = voteProposal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return id.equals(option.id) && name.equals(option.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Option o) {
        return Comparator.comparing(Option::getName, Comparator.naturalOrder()).compare(this, o);
    }
}
