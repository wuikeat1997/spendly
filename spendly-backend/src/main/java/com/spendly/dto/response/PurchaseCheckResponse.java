package com.spendly.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseCheckResponse(String id, BigDecimal amount, String verdict, String consequence,
		Instant checkedAt) {
}
