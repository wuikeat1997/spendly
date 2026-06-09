package com.spendly.service.impl;

import com.spendly.config.MailProperties;
import com.spendly.service.EmailDeliveryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailDeliveryService implements EmailDeliveryService {

	private final JavaMailSender mailSender;

	private final MailProperties mailProperties;

	public SmtpEmailDeliveryService(JavaMailSender mailSender, MailProperties mailProperties) {
		this.mailSender = mailSender;
		this.mailProperties = mailProperties;
	}

	@Override
	public void sendOtp(String email, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(mailProperties.from());
		message.setTo(email);
		message.setSubject("Your Spendly sign-in code");
		message.setText("""
				Your Spendly sign-in code is %s.

				This code expires in 10 minutes. If you did not request it, you can ignore this email.
				""".formatted(otp));
		mailSender.send(message);
	}

}
