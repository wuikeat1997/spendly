package com.spendly.dto.response;

import java.time.Instant;

public record AuthSessionDeviceResponse(String id, String deviceId, String deviceName, Instant lastUsedAt,
		Instant expiresAt, boolean currentDevice) {
}
