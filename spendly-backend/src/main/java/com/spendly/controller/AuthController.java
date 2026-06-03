package com.spendly.controller;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionResponse;
import com.spendly.dto.response.CurrentUserResponse;
import com.spendly.dto.response.SendOtpResponse;
import com.spendly.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
		return authService.verifyOtp(request, remoteAddress(servletRequest));
	}

	@PostMapping("/refresh")
	public AuthSessionResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@GetMapping("/me")
	public CurrentUserResponse me(Authentication authentication) {
		AuthenticatedUser user = authService.currentUser((AuthenticatedUser) authentication.getPrincipal());
		return new CurrentUserResponse(user.id(), user.refNo());
	}

	private String remoteAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("x-forwarded-for");
		if (forwardedFor == null || forwardedFor.isBlank()) {
			return request.getRemoteAddr();
		}

		return forwardedFor.split(",")[0].trim();
	}

}
