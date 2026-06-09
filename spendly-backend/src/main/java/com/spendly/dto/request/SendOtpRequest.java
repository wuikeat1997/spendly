package com.spendly.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendOtpRequest(
		@NotBlank(message = "Email is required.") @Email(message = "Email must be valid.") String email,

		String reason) {
}
