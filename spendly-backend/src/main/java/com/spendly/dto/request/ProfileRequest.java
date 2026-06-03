package com.spendly.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfileRequest(
		@NotNull(message = "Monthly income is required.") @DecimalMin(value = "0.00",
				message = "Monthly income cannot be negative.") BigDecimal monthlyIncome,
		@NotNull(message = "Monthly commitments are required.") @DecimalMin(value = "0.00",
				message = "Monthly commitments cannot be negative.") BigDecimal monthlyCommitments,
		@NotNull(message = "Current balance is required.") @DecimalMin(value = "0.00",
				message = "Current balance cannot be negative.") BigDecimal currentBalance,
		@NotNull(message = "Protected buffer is required.") @DecimalMin(value = "0.00",
				message = "Protected buffer cannot be negative.") BigDecimal protectedBuffer,
		@NotNull(message = "Last balance update is required.") LocalDate lastBalanceUpdate) {
}
