package com.spendly.repository;

import com.spendly.entity.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	Optional<RefreshToken> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(String tokenHash, Instant now);

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	List<RefreshToken> findByTokenFamilyIdAndRevokedAtIsNull(String tokenFamilyId);

	List<RefreshToken> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastUsedAtDesc(String userId, Instant now);

	Optional<RefreshToken> findByUserIdAndIdAndRevokedAtIsNull(String userId, String id);

}
