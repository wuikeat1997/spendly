package com.spendly.repository;

import com.spendly.entity.EmailOtp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, String> {

	Optional<EmailOtp> findFirstByEmailAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(String email,
			Instant now);

	@Modifying
	@Query("""
			UPDATE EmailOtp otp
			SET otp.consumedAt = :consumedAt
			WHERE otp.email = :email
			  AND otp.consumedAt IS NULL
			""")
	void consumeAllUsableForEmail(@Param("email") String email, @Param("consumedAt") Instant consumedAt);

}
