package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;
import java.util.Optional;

public interface TokenService {

	String createAccessToken(AuthenticatedUser user);

	long accessTokenExpiresInSeconds();

	long refreshTokenExpiresInSeconds();

	Optional<AuthenticatedUser> verifyAccessToken(String token);

}
