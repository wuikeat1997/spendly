package com.spendly.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimiter {

	private final Clock clock;

	private final Map<String, Entry> entries = new ConcurrentHashMap<>();

	public InMemoryRateLimiter(Clock clock) {
		this.clock = clock;
	}

	public Result consume(String key, int limit, Duration window) {
		long now = clock.millis();
		long windowMs = window.toMillis();

		Entry updated = entries.compute(key, (ignored, current) -> {
			if (current == null || current.resetAt <= now) {
				return new Entry(1, now + windowMs);
			}

			if (current.count >= limit) {
				return current;
			}

			return new Entry(current.count + 1, current.resetAt);
		});

		return new Result(updated.count <= limit, updated.count, limit, updated.resetAt);
	}

	private record Entry(int count, long resetAt) {
	}

	public record Result(boolean allowed, int count, int limit, long resetAt) {
	}

}
