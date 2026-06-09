package com.spendly.service.impl;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.PurchaseCheckRequest;
import com.spendly.dto.response.PurchaseCheckResponse;
import com.spendly.entity.PurchaseCheck;
import com.spendly.repository.PurchaseCheckRepository;
import com.spendly.service.PurchaseCheckService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseCheckServiceImpl implements PurchaseCheckService {

	private final PurchaseCheckRepository purchaseCheckRepository;

	public PurchaseCheckServiceImpl(PurchaseCheckRepository purchaseCheckRepository) {
		this.purchaseCheckRepository = purchaseCheckRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PurchaseCheckResponse> findRecentChecks(AuthenticatedUser user) {
		return purchaseCheckRepository.findTop8ByUserIdOrderByCheckedAtDesc(user.id())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	@Transactional
	public PurchaseCheckResponse saveCheck(AuthenticatedUser user, PurchaseCheckRequest request) {
		PurchaseCheck check = new PurchaseCheck(user.id(), request.amount(), request.verdict(), request.consequence(),
				request.checkedAt());
		return toResponse(purchaseCheckRepository.save(check));
	}

	private PurchaseCheckResponse toResponse(PurchaseCheck check) {
		return new PurchaseCheckResponse(check.getId(), check.getAmount(), check.getVerdict(), check.getConsequence(),
				check.getCheckedAt());
	}

}
