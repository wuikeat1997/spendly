package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.RefreshTokenRequest;
import com.spendly.dto.request.SendOtpRequest;
import com.spendly.dto.request.VerifyOtpRequest;
import com.spendly.dto.response.AuthSessionResponse;
import com.spendly.dto.response.SendOtpResponse;

public interface AuthService {

	SendOtpResponse sendOtp(SendOtpRequest request, String remoteAddress);

	AuthSessionResponse verifyOtp(VerifyOtpRequest request, String remoteAddress);

	AuthSessionResponse refresh(RefreshTokenRequest request);

	AuthenticatedUser currentUser(AuthenticatedUser user);

}
