package com.spendly.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfileResponse(BigDecimal monthlyIncome, BigDecimal monthlyCommitments, BigDecimal currentBalance,
		BigDecimal protectedBuffer, LocalDate lastBalanceUpdate) {
}
