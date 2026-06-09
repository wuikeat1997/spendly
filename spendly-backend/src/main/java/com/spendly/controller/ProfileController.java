package com.spendly.controller;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.ProfileRequest;
import com.spendly.dto.response.ProfileResponse;
import com.spendly.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
		return profileService.findProfile(currentUser(authentication))
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping
	public ProfileResponse saveProfile(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
		return profileService.saveProfile(currentUser(authentication), request);
	}

	private AuthenticatedUser currentUser(Authentication authentication) {
		return (AuthenticatedUser) authentication.getPrincipal();
	}

}
