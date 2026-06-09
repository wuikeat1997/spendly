package com.spendly.controller;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.response.UserDataResponse;
import com.spendly.service.UserDataService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/data")
public class UserDataController {

	private final UserDataService userDataService;

	public UserDataController(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	@DeleteMapping
	public UserDataResponse clearData(Authentication authentication) {
		userDataService.clearUserData((AuthenticatedUser) authentication.getPrincipal());
		return new UserDataResponse(true);
	}

}
