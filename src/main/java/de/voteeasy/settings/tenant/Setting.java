package de.voteeasy.settings.tenant;

import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Getter
@Setter
@Entity
public class Setting implements Serializable, Comparable<Setting> {
    @Serial
    private static final long serialVersionUID = -6325507628981867763L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String key;

    @Column(length = 100, nullable = false)
    private String value;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SettingScope scope = SettingScope.TENANT;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettingType type;

    @Column
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    private User createdBy;

    // explicitly set to eager to avoid no session errors
    @OneToOne(fetch = FetchType.EAGER)
    private Tenant tenant;

    public Setting() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Setting setting = (Setting) o;
        return Objects.equals(id, setting.id) && Objects.equals(key, setting.key) && Objects.equals(value, setting.value) && scope == setting.scope && type == setting.type && Objects.equals(createdAt, setting.createdAt) && Objects.equals(createdBy, setting.createdBy) && Objects.equals(tenant, setting.tenant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, value, scope, type, createdAt, createdBy, tenant);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Setting{");
        sb.append("id=").append(id);
        sb.append(", key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", scope=").append(scope);
        sb.append(", type=").append(type);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", tenant=").append(tenant);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Setting o) {
        return Comparator.comparing(Setting::getType).thenComparing(Setting::getKey).thenComparing(Setting::getScope).compare(this, o);
    }
}
