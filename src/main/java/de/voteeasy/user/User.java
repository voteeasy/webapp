package de.voteeasy.user;

import de.voteeasy.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_name", "user_name"})
)
public class User implements Serializable, Comparable<User> {
    @Serial
    private static final long serialVersionUID = -7005112921626299481L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(length = 100, nullable = false)
    private String userName;
    @Column(length = 100, nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String passwordHash;
    @Column
    private String resetToken;
    @Builder.Default
    @Column
    private Long createdAt = System.currentTimeMillis();

    // to allow for later administration
    @Column
    @Builder.Default
    private boolean enabled = true;

    // explicitly set to eager to avoid no session errors
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Builder.Default
    private Collection<Role> roles = new HashSet<>();

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_name")
    private Tenant organisation;

    public User() {

    }

    public User(UUID id, String userName, String fullName, String passwordHash, Long createdAt, boolean enabled, Collection<Role> roles, Tenant organisation) {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.enabled = enabled;
        this.roles = roles;
        this.organisation = organisation;
    }

    public User(String userName, String fullName, String passwordHash, Tenant organisation) {
        this.userName = userName;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.organisation = organisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(resetToken, user.resetToken) && enabled == user.enabled && Objects.equals(id, user.id) && Objects.equals(userName, user.userName) && Objects.equals(fullName, user.fullName) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, fullName, passwordHash, createdAt, enabled);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append(", passwordHash='").append(passwordHash).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", enabled=").append(enabled);
        sb.append(", resetToken=").append(resetToken);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(User o) {
        return Comparator.comparing(User::getFullName).thenComparing(User::getUserName).thenComparing(User::getCreatedAt).compare(this, o);
    }
}
