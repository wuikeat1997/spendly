package com.spendly.entity;

import com.spendly.constant.ColumnLengthConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Profile {

	private static final String DEFAULT_AUDIT_REF_DEFINITION = "VARCHAR("
			+ ColumnLengthConstant.DEFAULT_AUDIT_REF_LENGTH + ") DEFAULT 'sys-admin'";

	@Id
	@Column(name = "user_id", nullable = false, length = ColumnLengthConstant.DEFAULT_ID_LENGTH)
	private String userId;

	@Column(name = "monthly_income", nullable = false, precision = 12, scale = 2)
	private BigDecimal monthlyIncome;

	@Column(name = "monthly_commitments", nullable = false, precision = 12, scale = 2)
	private BigDecimal monthlyCommitments;

	@Column(name = "current_balance", nullable = false, precision = 12, scale = 2)
	private BigDecimal currentBalance;

	@Column(name = "protected_buffer", nullable = false, precision = 12, scale = 2)
	private BigDecimal protectedBuffer;

	@Column(name = "last_balance_update", nullable = false)
	private LocalDate lastBalanceUpdate;

	@Setter(AccessLevel.PRIVATE)
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false,
			columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
	private Instant createdAt = Instant.now();

	@Setter(AccessLevel.PRIVATE)
	@CreatedBy
	@Column(name = "created_by", nullable = false, updatable = false, columnDefinition = DEFAULT_AUDIT_REF_DEFINITION)
	private String createdBy;

	@Setter(AccessLevel.PRIVATE)
	@LastModifiedDate
	@Column(name = "last_modified_at", nullable = false,
			columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
	private Instant lastModifiedAt = Instant.now();

	@Setter(AccessLevel.PRIVATE)
	@LastModifiedBy
	@Column(name = "last_modified_by", nullable = false, columnDefinition = DEFAULT_AUDIT_REF_DEFINITION)
	private String lastModifiedBy;

	public Profile(String userId, BigDecimal monthlyIncome, BigDecimal monthlyCommitments, BigDecimal currentBalance,
			BigDecimal protectedBuffer, LocalDate lastBalanceUpdate) {
		this.userId = userId;
		this.monthlyIncome = monthlyIncome;
		this.monthlyCommitments = monthlyCommitments;
		this.currentBalance = currentBalance;
		this.protectedBuffer = protectedBuffer;
		this.lastBalanceUpdate = lastBalanceUpdate;
	}

}
