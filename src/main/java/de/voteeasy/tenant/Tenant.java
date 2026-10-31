package de.voteeasy.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

// a.k.a Group
@Entity
@Getter
@Setter
public class Tenant implements Serializable, Comparable<Tenant> {
    @Serial
    private static final long serialVersionUID = -4133971026892203708L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column
    private Long createdAt = System.currentTimeMillis();

    // to allow for later administration
    @Column
    private boolean enabled = true;

    public Tenant() {

    }

    public Tenant(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return createdAt.equals(tenant.createdAt) && enabled == tenant.enabled && id.equals(tenant.id) && name.equals(tenant.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, enabled, createdAt);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tenant{");
        sb.append("id=").append(id);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", enabled=").append(enabled);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Tenant o) {
        return Comparator.comparing(Tenant::getName).thenComparing(Tenant::getCreatedAt).thenComparing(Tenant::isEnabled).compare(this, o);
    }
}
