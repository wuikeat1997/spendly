package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.PurchaseCheckRequest;
import com.spendly.dto.response.PurchaseCheckResponse;
import java.util.List;

public interface PurchaseCheckService {

	List<PurchaseCheckResponse> findRecentChecks(AuthenticatedUser user);

	PurchaseCheckResponse saveCheck(AuthenticatedUser user, PurchaseCheckRequest request);

}
