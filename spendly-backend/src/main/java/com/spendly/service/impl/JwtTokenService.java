package com.spendly.service.impl;

import com.spendly.config.JwtProperties;
import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements TokenService {

	private final JwtProperties jwtProperties;

	private final SecretKey secretKey;

	public JwtTokenService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String createAccessToken(AuthenticatedUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(accessTokenExpiresInSeconds());

		return Jwts.builder()
			.subject(user.id())
			.claim("refNo", user.refNo())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey)
			.compact();
	}

	@Override
	public long accessTokenExpiresInSeconds() {
		return jwtProperties.accessTokenMinutes() * 60;
	}

	@Override
	public long refreshTokenExpiresInSeconds() {
		return jwtProperties.refreshTokenDays() * 24 * 60 * 60;
	}

	@Override
	public Optional<AuthenticatedUser> verifyAccessToken(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

			return Optional.of(new AuthenticatedUser(claims.getSubject(), claims.get("refNo", String.class)));
		}
		catch (IllegalArgumentException | JwtException error) {
			return Optional.empty();
		}
	}

}
