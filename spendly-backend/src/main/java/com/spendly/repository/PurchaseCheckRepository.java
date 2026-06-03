package com.spendly.repository;

import com.spendly.entity.PurchaseCheck;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseCheckRepository extends JpaRepository<PurchaseCheck, String> {

	List<PurchaseCheck> findTop8ByUserIdOrderByCheckedAtDesc(String userId);

	void deleteByUserId(String userId);

}
