package com.spendly.service.impl;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.ProfileRequest;
import com.spendly.dto.response.ProfileResponse;
import com.spendly.entity.Profile;
import com.spendly.repository.ProfileRepository;
import com.spendly.service.ProfileService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

	private final ProfileRepository profileRepository;

	public ProfileServiceImpl(ProfileRepository profileRepository) {
		this.profileRepository = profileRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ProfileResponse> findProfile(AuthenticatedUser user) {
		return profileRepository.findById(user.id()).map(this::toResponse);
	}

	@Override
	@Transactional
	public ProfileResponse saveProfile(AuthenticatedUser user, ProfileRequest request) {
		Profile profile = profileRepository.findById(user.id()).orElseGet(Profile::new);
		profile.setUserId(user.id());
		profile.setMonthlyIncome(request.monthlyIncome());
		profile.setMonthlyCommitments(request.monthlyCommitments());
		profile.setCurrentBalance(request.currentBalance());
		profile.setProtectedBuffer(request.protectedBuffer());
		profile.setLastBalanceUpdate(request.lastBalanceUpdate());

		return toResponse(profileRepository.save(profile));
	}

	private ProfileResponse toResponse(Profile profile) {
		return new ProfileResponse(profile.getMonthlyIncome(), profile.getMonthlyCommitments(),
				profile.getCurrentBalance(), profile.getProtectedBuffer(), profile.getLastBalanceUpdate());
	}

}
