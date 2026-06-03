package com.spendly.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseCheckRequest(
		@NotNull(message = "Amount is required.") @DecimalMin(value = "0.01",
				message = "Amount must be greater than zero.") BigDecimal amount,
		@NotBlank(message = "Verdict is required.") @Size(max = 20,
				message = "Verdict must be 20 characters or fewer.") @Pattern(regexp = "Safe|Risky|Not safe",
						message = "Verdict must be Safe, Risky, or Not safe.") String verdict,
		@NotBlank(message = "Consequence is required.") String consequence,
		@NotNull(message = "Checked at is required.") Instant checkedAt) {
}
