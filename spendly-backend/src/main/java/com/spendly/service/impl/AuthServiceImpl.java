package com.spendly.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.spendly.config.OtpProperties;
import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.SignOutRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionDeviceResponse;
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
import java.util.List;
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
	public AuthSessionResponse verifyOtp(VerifyOtpRequest request, String remoteAddress, String userAgent) {
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

		return createSession(new AuthenticatedUser(user.getId(), user.getRefNo()), generateUlid(),
				normalizeDeviceId(request.deviceId()), request.deviceName(), userAgent);
	}

	@Override
	@Transactional(noRollbackFor = AuthException.class)
	public AuthSessionResponse refresh(RefreshTokenRequest request, String userAgent) {
		String tokenHash = sha256(request.refreshToken());
		Instant now = Instant.now(clock);
		RefreshToken refreshToken = refreshTokenRepository
			.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)
			.orElseGet(() -> handleInvalidRefreshToken(tokenHash, now));

		refreshToken.setRevokedAt(now);
		AppUser user = appUserRepository.findById(refreshToken.getUserId())
			.orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_MESSAGE));
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), user.getRefNo());
		SessionToken sessionToken = createRefreshToken(authenticatedUser, refreshToken.getTokenFamilyId(),
				normalizeDeviceId(request.deviceId(), refreshToken.getDeviceId()),
				firstNonBlank(request.deviceName(), refreshToken.getDeviceName()),
				firstNonBlank(userAgent, refreshToken.getUserAgent()));

		refreshToken.setReplacedByTokenId(sessionToken.entity().getId());
		refreshTokenRepository.save(refreshToken);

		return toSessionResponse(authenticatedUser, sessionToken.refreshToken());
	}

	@Override
	@Transactional
	public void signOut(SignOutRequest request) {
		String tokenHash = sha256(request.refreshToken());
		Instant now = Instant.now(clock);
		refreshTokenRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now).ifPresent(token -> {
			token.setRevokedAt(now);
			refreshTokenRepository.save(token);
		});
	}

	@Override
	public AuthenticatedUser currentUser(AuthenticatedUser user) {
		return appUserRepository.findById(user.id())
			.map(found -> new AuthenticatedUser(found.getId(), found.getRefNo()))
			.orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "User is no longer active."));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AuthSessionDeviceResponse> listSessions(AuthenticatedUser user, String currentDeviceId) {
		Instant now = Instant.now(clock);
		return refreshTokenRepository
			.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastUsedAtDesc(user.id(), now)
			.stream()
			.map(token -> new AuthSessionDeviceResponse(token.getId(), token.getDeviceId(), token.getDeviceName(),
					token.getLastUsedAt(), token.getExpiresAt(), token.getDeviceId().equals(currentDeviceId)))
			.toList();
	}

	@Override
	@Transactional
	public void revokeSession(AuthenticatedUser user, String sessionId) {
		RefreshToken token = refreshTokenRepository.findByUserIdAndIdAndRevokedAtIsNull(user.id(), sessionId)
			.orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "Session was not found."));
		token.setRevokedAt(Instant.now(clock));
		refreshTokenRepository.save(token);
	}

	private AuthSessionResponse createSession(AuthenticatedUser user, String tokenFamilyId, String deviceId,
			String deviceName, String userAgent) {
		SessionToken sessionToken = createRefreshToken(user, tokenFamilyId, deviceId, deviceName, userAgent);
		return toSessionResponse(user, sessionToken.refreshToken());
	}

	private SessionToken createRefreshToken(AuthenticatedUser user, String tokenFamilyId, String deviceId,
			String deviceName, String userAgent) {
		String refreshToken = generateRefreshToken();
		Instant refreshExpiresAt = Instant.now(clock).plusSeconds(tokenService.refreshTokenExpiresInSeconds());
		RefreshToken entity = refreshTokenRepository
			.save(new RefreshToken(user.id(), sha256(refreshToken), tokenFamilyId, deviceId, truncate(deviceName, 120),
					truncate(userAgent, 255), refreshExpiresAt, Instant.now(clock)));

		return new SessionToken(refreshToken, entity);
	}

	private AuthSessionResponse toSessionResponse(AuthenticatedUser user, String refreshToken) {
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

	private String generateUlid() {
		return UlidCreator.getUlid().toString();
	}

	private String generateRefreshToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private RefreshToken handleInvalidRefreshToken(String tokenHash, Instant now) {
		refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(found -> {
			if (found.getRevokedAt() != null && found.getReuseDetectedAt() == null) {
				found.setReuseDetectedAt(now);
				refreshTokenRepository.save(found);
				revokeTokenFamily(found.getTokenFamilyId(), now);
			}
		});

		throw new AuthException(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_MESSAGE);
	}

	private void revokeTokenFamily(String tokenFamilyId, Instant now) {
		refreshTokenRepository.findByTokenFamilyIdAndRevokedAtIsNull(tokenFamilyId).forEach(token -> {
			token.setRevokedAt(now);
			refreshTokenRepository.save(token);
		});
	}

	private String normalizeDeviceId(String deviceId) {
		return normalizeDeviceId(deviceId, generateUlid());
	}

	private String normalizeDeviceId(String deviceId, String fallback) {
		if (deviceId == null || deviceId.isBlank()) {
			return fallback;
		}

		return truncate(deviceId.trim(), 64);
	}

	private String firstNonBlank(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength);
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

	private record SessionToken(String refreshToken, RefreshToken entity) {
	}

}
