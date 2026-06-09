package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;

public interface UserDataService {

	void clearUserData(AuthenticatedUser user);

}
