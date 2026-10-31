package de.voteeasy.invitation;

import de.voteeasy.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Getter
@Setter
@Table
public class UserToken implements Serializable, Comparable<UserToken> {
    @Serial
    private static final long serialVersionUID = 2687041645189990556L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 50, nullable = false, unique = true)
    private String token;

    @Builder.Default
    @Column
    private Long createdAt = System.currentTimeMillis();

    @Column
    private Long redeemedAt;

    // to allow for later administration
    @Builder.Default
    @Column
    private boolean enabled = true;

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_name")
    private Tenant organisation;

    public UserToken() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserToken userToken = (UserToken) o;
        return enabled == userToken.enabled && id.equals(userToken.id) && token.equals(userToken.token) && createdAt.equals(userToken.createdAt) && Objects.equals(redeemedAt, userToken.redeemedAt) && organisation.equals(userToken.organisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, createdAt, redeemedAt, enabled, organisation);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserToken{");
        sb.append("id=").append(id);
        sb.append(", token='").append(token).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", redeemedAt=").append(redeemedAt);
        sb.append(", enabled=").append(enabled);
        sb.append(", organisation=").append(organisation);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(UserToken o) {
        return Comparator.comparing(UserToken::getOrganisation).thenComparing(UserToken::getToken).thenComparing(UserToken::getCreatedAt).compare(this, o);
    }
}
