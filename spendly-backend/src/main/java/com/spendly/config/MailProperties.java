package com.spendly.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String provider, String from, Resend resend) {

	public record Resend(String apiKey, String apiUrl) {
	}

}
