package com.spendly.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignOutRequest(@NotBlank(message = "Refresh token is required.") String refreshToken) {
}
