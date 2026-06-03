package com.spendly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_checks")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseCheck extends AuditSection {

	@Column(name = "user_id", nullable = false, length = 26)
	private String userId;

	@Column(name = "amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "verdict", nullable = false, length = 20)
	private String verdict;

	@Column(name = "consequence", nullable = false, length = 1000)
	private String consequence;

	@Column(name = "checked_at", nullable = false)
	private Instant checkedAt;

	public PurchaseCheck(String userId, BigDecimal amount, String verdict, String consequence, Instant checkedAt) {
		this.userId = userId;
		this.amount = amount;
		this.verdict = verdict;
		this.consequence = consequence;
		this.checkedAt = checkedAt;
	}

}
