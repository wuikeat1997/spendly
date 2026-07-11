package com.spendly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(@NotBlank(message = "Refresh token is required.") String refreshToken,

		@Size(max = 64, message = "Device id must be 64 characters or fewer.") String deviceId,

		@Size(max = 120, message = "Device name must be 120 characters or fewer.") String deviceName) {
}
