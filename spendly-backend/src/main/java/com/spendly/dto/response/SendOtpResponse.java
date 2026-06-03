package com.spendly.dto.response;

public record SendOtpResponse(boolean ok, int requestCount, int requestLimit, long resetAt) {
}
