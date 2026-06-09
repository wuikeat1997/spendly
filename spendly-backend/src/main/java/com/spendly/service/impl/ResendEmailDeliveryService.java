package com.spendly.service.impl;

import com.spendly.config.MailProperties;
import com.spendly.service.EmailDeliveryService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "resend")
public class ResendEmailDeliveryService implements EmailDeliveryService {

	private final RestClient restClient;

	private final MailProperties mailProperties;

	public ResendEmailDeliveryService(RestClient.Builder restClientBuilder, MailProperties mailProperties) {
		this.restClient = restClientBuilder.build();
		this.mailProperties = mailProperties;
	}

	@Override
	public void sendOtp(String email, String otp) {
		MailProperties.Resend resend = mailProperties.resend();
		if (resend == null || resend.apiKey() == null || resend.apiKey().isBlank()) {
			throw new IllegalStateException("RESEND_API_KEY is required when app.mail.provider=resend.");
		}

		restClient.post()
			.uri(resend.apiUrl())
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + resend.apiKey())
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ResendEmailRequest(mailProperties.from(), List.of(email), "Your Spendly sign-in code", """
					Your Spendly sign-in code is %s.

					This code expires in 10 minutes. If you did not request it, you can ignore this email.
					""".formatted(otp)))
			.retrieve()
			.toBodilessEntity();
	}

	private record ResendEmailRequest(String from, List<String> to, String subject, String text) {
	}

}
