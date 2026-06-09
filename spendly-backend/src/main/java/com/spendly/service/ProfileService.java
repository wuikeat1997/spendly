package com.spendly.service;

import com.spendly.dto.model.AuthenticatedUser;
import com.spendly.dto.request.ProfileRequest;
import com.spendly.dto.response.ProfileResponse;
import java.util.Optional;

public interface ProfileService {

	Optional<ProfileResponse> findProfile(AuthenticatedUser user);

	ProfileResponse saveProfile(AuthenticatedUser user, ProfileRequest request);

}
