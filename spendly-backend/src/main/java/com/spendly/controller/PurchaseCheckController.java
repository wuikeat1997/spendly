package com.spendly.controller;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.PurchaseCheckRequest;
import com.spendly.dto.response.PurchaseCheckResponse;
import com.spendly.service.PurchaseCheckService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-checks")
public class PurchaseCheckController {

	private final PurchaseCheckService purchaseCheckService;

	public PurchaseCheckController(PurchaseCheckService purchaseCheckService) {
		this.purchaseCheckService = purchaseCheckService;
	}

	@GetMapping
	public List<PurchaseCheckResponse> findRecentChecks(Authentication authentication) {
		return purchaseCheckService.findRecentChecks(currentUser(authentication));
	}

	@PostMapping
	public PurchaseCheckResponse saveCheck(Authentication authentication,
			@Valid @RequestBody PurchaseCheckRequest request) {
		return purchaseCheckService.saveCheck(currentUser(authentication), request);
	}

	private AuthenticatedUser currentUser(Authentication authentication) {
		return (AuthenticatedUser) authentication.getPrincipal();
	}

}
