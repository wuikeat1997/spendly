package com.spendly.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
		@NotBlank(message = "Email is required.") @Email(message = "Email must be valid.") String email,

		@NotBlank(message = "OTP is required.") @Pattern(regexp = "^[0-9]{6}$",
				message = "OTP must be exactly 6 digits.") String token,

		@Size(max = 64, message = "Device id must be 64 characters or fewer.") String deviceId,

		@Size(max = 120, message = "Device name must be 120 characters or fewer.") String deviceName) {
}
