package com.spendly.service.impl;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.repository.ProfileRepository;
import com.spendly.repository.PurchaseCheckRepository;
import com.spendly.service.UserDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDataServiceImpl implements UserDataService {

	private final ProfileRepository profileRepository;

	private final PurchaseCheckRepository purchaseCheckRepository;

	public UserDataServiceImpl(ProfileRepository profileRepository, PurchaseCheckRepository purchaseCheckRepository) {
		this.profileRepository = profileRepository;
		this.purchaseCheckRepository = purchaseCheckRepository;
	}

	@Override
	@Transactional
	public void clearUserData(AuthenticatedUser user) {
		purchaseCheckRepository.deleteByUserId(user.id());
		profileRepository.deleteById(user.id());
	}

}
