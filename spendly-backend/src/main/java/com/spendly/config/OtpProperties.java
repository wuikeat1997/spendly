package com.spendly.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(long expiryMinutes, int sendLimit, long sendWindowSeconds, int verifyLimit,
		long verifyWindowMinutes) {
}
