package com.spendly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends AuditSection {

	@Column(name = "user_id", nullable = false, length = 26)
	private String userId;

	@Column(name = "token_hash", nullable = false)
	private String tokenHash;

	@Column(name = "token_family_id", nullable = false, length = 26)
	private String tokenFamilyId;

	@Column(name = "device_id", nullable = false, length = 64)
	private String deviceId;

	@Column(name = "device_name", length = 120)
	private String deviceName;

	@Column(name = "user_agent", length = 255)
	private String userAgent;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "last_used_at", nullable = false)
	private Instant lastUsedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "replaced_by_token_id", length = 26)
	private String replacedByTokenId;

	@Column(name = "reuse_detected_at")
	private Instant reuseDetectedAt;

	public RefreshToken(String userId, String tokenHash, String tokenFamilyId, String deviceId, String deviceName,
			String userAgent, Instant expiresAt, Instant lastUsedAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.tokenFamilyId = tokenFamilyId;
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.userAgent = userAgent;
		this.expiresAt = expiresAt;
		this.lastUsedAt = lastUsedAt;
	}

}
