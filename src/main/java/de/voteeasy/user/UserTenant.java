package de.voteeasy.user;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@ToString
public class UserTenant implements Serializable {
	@Serial
	private static final long serialVersionUID = 9136872826771622827L;
	private String tenantName;
    private UUID tenantId;
}
