package com.spendly.audit;

import com.spendly.dto.model.AuthenticatedUser;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<String> {

	private static final String DEFAULT_AUDITOR = "sys-admin";

	@Override
	public @NonNull Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
			return Optional.of(user.refNo());
		}

		return Optional.of(DEFAULT_AUDITOR);
	}

}
