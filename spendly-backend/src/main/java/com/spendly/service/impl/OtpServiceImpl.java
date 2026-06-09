package com.spendly.service.impl;

import com.spendly.service.OtpService;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate() {
		return "%06d".formatted(secureRandom.nextInt(1_000_000));
	}

}
