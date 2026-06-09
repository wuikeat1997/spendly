package com.spendly.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.spendly.config.OtpProperties;
import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionResponse;
import com.spendly.dto.response.SendOtpResponse;
import com.spendly.entity.AppUser;
import com.spendly.entity.EmailOtp;
import com.spendly.entity.RefreshToken;
import com.spendly.exception.AuthException;
import com.spendly.repository.AppUserRepository;
import com.spendly.repository.EmailOtpRepository;
import com.spendly.repository.RefreshTokenRepository;
import com.spendly.service.AuthService;
import com.spendly.service.EmailDeliveryService;
import com.spendly.service.OtpService;
import com.spendly.service.TokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String INVALID_OTP_MESSAGE = "Invalid or expired OTP.";

	private static final String INVALID_REFRESH_MESSAGE = "Invalid or expired refresh token.";

	private final AppUserRepository appUserRepository;

	private final EmailOtpRepository emailOtpRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final EmailDeliveryService emailDeliveryService;

	private final OtpService otpService;

	private final TokenService tokenService;

	private final PasswordEncoder passwordEncoder;

	private final OtpProperties otpProperties;

	private final InMemoryRateLimiter rateLimiter;

	private final Clock clock;

	private final SecureRandom secureRandom = new SecureRandom();

	public AuthServiceImpl(AppUserRepository appUserRepository, EmailOtpRepository emailOtpRepository,
			RefreshTokenRepository refreshTokenRepository, EmailDeliveryService emailDeliveryService,
			OtpService otpService, TokenService tokenService, PasswordEncoder passwordEncoder,
			OtpProperties otpProperties, InMemoryRateLimiter rateLimiter, Clock clock) {
		this.appUserRepository = appUserRepository;
		this.emailOtpRepository = emailOtpRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.emailDeliveryService = emailDeliveryService;
		this.otpService = otpService;
		this.tokenService = tokenService;
		this.passwordEncoder = passwordEncoder;
		this.otpProperties = otpProperties;
		this.rateLimiter = rateLimiter;
		this.clock = clock;
	}

	@Override
	@Transactional
	public SendOtpResponse sendOtp(SendOtpRequest request, String remoteAddress) {
		String email = normalizeEmail(request.email());
		InMemoryRateLimiter.Result limit = rateLimiter.consume("send:%s:%s".formatted(email, remoteAddress),
				otpProperties.sendLimit(), Duration.ofSeconds(otpProperties.sendWindowSeconds()));

		if (!limit.allowed()) {
			throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "Too many sign-in emails. Try again later.");
		}

		String otp = otpService.generate();
		Instant expiresAt = Instant.now(clock).plus(Duration.ofMinutes(otpProperties.expiryMinutes()));
		emailOtpRepository.save(new EmailOtp(email, passwordEncoder.encode(otp), expiresAt));
		emailDeliveryService.sendOtp(email, otp);

		return new SendOtpResponse(true, limit.count(), limit.limit(), limit.resetAt());
	}

	@Override
	@Transactional
	public AuthSessionResponse verifyOtp(VerifyOtpRequest request, String remoteAddress) {
		String email = normalizeEmail(request.email());
		InMemoryRateLimiter.Result limit = rateLimiter.consume("verify:%s:%s".formatted(email, remoteAddress),
				otpProperties.verifyLimit(), Duration.ofMinutes(otpProperties.verifyWindowMinutes()));

		if (!limit.allowed()) {
			throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "Too many OTP attempts. Try again later.");
		}

		Instant now = Instant.now(clock);
		EmailOtp otp = emailOtpRepository
			.findFirstByEmailAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(email, now)
			.orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, INVALID_OTP_MESSAGE));

		if (!passwordEncoder.matches(request.token(), otp.getTokenHash())) {
			throw new AuthException(HttpStatus.BAD_REQUEST, INVALID_OTP_MESSAGE);
		}

		emailOtpRepository.consumeAllUsableForEmail(email, now);
		AppUser user = appUserRepository.findByEmail(email).orElseGet(() -> createUser(email));

		return createSession(new AuthenticatedUser(user.getId(), user.getRefNo()));
	}

	@Override
	@Transactional
	public AuthSessionResponse refresh(RefreshTokenRequest request) {
		String tokenHash = sha256(request.refreshToken());
		Instant now = Instant.now(clock);
		RefreshToken refreshToken = refreshTokenRepository
			.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)
			.orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_MESSAGE));

		refreshToken.setRevokedAt(now);
		refreshTokenRepository.save(refreshToken);
		AppUser user = appUserRepository.findById(refreshToken.getUserId())
			.orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_MESSAGE));

		return createSession(new AuthenticatedUser(user.getId(), user.getRefNo()));
	}

	@Override
	public AuthenticatedUser currentUser(AuthenticatedUser user) {
		return appUserRepository.findById(user.id())
			.map(found -> new AuthenticatedUser(found.getId(), found.getRefNo()))
			.orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "User is no longer active."));
	}

	private AuthSessionResponse createSession(AuthenticatedUser user) {
		String refreshToken = generateRefreshToken();
		Instant refreshExpiresAt = Instant.now(clock).plusSeconds(tokenService.refreshTokenExpiresInSeconds());
		refreshTokenRepository.save(new RefreshToken(user.id(), sha256(refreshToken), refreshExpiresAt));

		return new AuthSessionResponse(tokenService.createAccessToken(user), "Bearer",
				tokenService.accessTokenExpiresInSeconds(), refreshToken, tokenService.refreshTokenExpiresInSeconds(),
				user);
	}

	private AppUser createUser(String email) {
		return appUserRepository.save(new AppUser(email, generateUserRefNo()));
	}

	private String generateUserRefNo() {
		return "USR" + UlidCreator.getUlid().toString();
	}

	private String generateRefreshToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException error) {
			throw new IllegalStateException("SHA-256 is unavailable.", error);
		}
	}

}
