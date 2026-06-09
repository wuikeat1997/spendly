package com.spendly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_otps")
@Getter
@Setter
@NoArgsConstructor
public class EmailOtp extends AuditSection {

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "token_hash", nullable = false)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "consumed_at")
	private Instant consumedAt;

	public EmailOtp(String email, String tokenHash, Instant expiresAt) {
		this.email = email;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

}
