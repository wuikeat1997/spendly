package com.spendly.controller;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.SignOutRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionDeviceResponse;
import com.spendly.dto.response.AuthSessionResponse;
import com.spendly.dto.response.CurrentUserResponse;
import com.spendly.dto.response.SendOtpResponse;
import com.spendly.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/send-otp")
	public SendOtpResponse sendOtp(@Valid @RequestBody SendOtpRequest request, HttpServletRequest servletRequest) {
		return authService.sendOtp(request, remoteAddress(servletRequest));
	}

	@PostMapping("/verify-otp")
	public AuthSessionResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request,
			HttpServletRequest servletRequest) {
		return authService.verifyOtp(request, remoteAddress(servletRequest), userAgent(servletRequest));
	}

	@PostMapping("/refresh")
	public AuthSessionResponse refresh(@Valid @RequestBody RefreshTokenRequest request,
			HttpServletRequest servletRequest) {
		return authService.refresh(request, userAgent(servletRequest));
	}

	@PostMapping("/sign-out")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void signOut(@Valid @RequestBody SignOutRequest request) {
		authService.signOut(request);
	}

	@GetMapping("/me")
	public CurrentUserResponse me(Authentication authentication) {
		AuthenticatedUser user = authService.currentUser((AuthenticatedUser) authentication.getPrincipal());
		return new CurrentUserResponse(user.id(), user.refNo());
	}

	@GetMapping("/sessions")
	public List<AuthSessionDeviceResponse> sessions(Authentication authentication,
			@RequestParam(required = false) String currentDeviceId) {
		AuthenticatedUser user = authService.currentUser((AuthenticatedUser) authentication.getPrincipal());
		return authService.listSessions(user, currentDeviceId);
	}

	@DeleteMapping("/sessions/{sessionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void revokeSession(Authentication authentication, @PathVariable String sessionId) {
		AuthenticatedUser user = authService.currentUser((AuthenticatedUser) authentication.getPrincipal());
		authService.revokeSession(user, sessionId);
	}

	private String remoteAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("x-forwarded-for");
		if (forwardedFor == null || forwardedFor.isBlank()) {
			return request.getRemoteAddr();
		}

		return forwardedFor.split(",")[0].trim();
	}

	private String userAgent(HttpServletRequest request) {
		return request.getHeader("user-agent");
	}

}
