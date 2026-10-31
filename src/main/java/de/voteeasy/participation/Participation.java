package de.voteeasy.participation;


import de.voteeasy.gathering.Gathering;
import de.voteeasy.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Participation implements Serializable, Comparable<Participation> {
	@Serial
    private static final long serialVersionUID = -5043176414983231380L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

	@Column
    private Long startedAt = System.currentTimeMillis();
    @Column
    private Long stoppedAt;

    @OneToOne(fetch = FetchType.EAGER)
    private User owner;

    @OneToOne(fetch = FetchType.EAGER)
    private Gathering event;

    public Participation() {

    }

    public Participation(User owner, Gathering event) {
        this.owner = owner;
        this.event = event;
    }

    @Override
    public int compareTo(Participation o) {
        return Comparator.comparing(Participation::getStartedAt).compare(this, o);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Participation{");
        sb.append("id=").append(id);
        sb.append(", startedAt=").append(startedAt);
        sb.append(", stoppedAt=").append(stoppedAt);
        sb.append(", owner=").append(owner);
        sb.append(", event=").append(event);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participation that = (Participation) o;
        return id.equals(that.id) && startedAt.equals(that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startedAt);
    }
}