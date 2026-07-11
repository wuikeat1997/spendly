package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.SignOutRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionDeviceResponse;
import com.spendly.dto.response.AuthSessionResponse;
import com.spendly.dto.response.SendOtpResponse;
import java.util.List;

public interface AuthService {

	SendOtpResponse sendOtp(SendOtpRequest request, String remoteAddress);

	AuthSessionResponse verifyOtp(VerifyOtpRequest request, String remoteAddress, String userAgent);

	AuthSessionResponse refresh(RefreshTokenRequest request, String userAgent);

	void signOut(SignOutRequest request);

	AuthenticatedUser currentUser(AuthenticatedUser user);

	List<AuthSessionDeviceResponse> listSessions(AuthenticatedUser user, String currentDeviceId);

	void revokeSession(AuthenticatedUser user, String sessionId);

}
