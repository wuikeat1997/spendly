package com.spendly.dto.response;

import com.spendly.dto.model.AuthenticatedUser;

public record AuthSessionResponse(String accessToken, String tokenType, long expiresIn, String refreshToken,
		long refreshExpiresIn, AuthenticatedUser user) {
}
