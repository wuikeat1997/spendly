package com.spendly.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendly.service.EmailDeliveryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class UserDataControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	@Test
	void savesProfileAndPurchaseChecksForCurrentUser() throws Exception {
		String accessToken = authenticate("data@spendly.com");

		mockMvc
			.perform(put("/api/profile").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "monthlyIncome": 5000,
						  "monthlyCommitments": 2200,
						  "currentBalance": 1800.50,
						  "protectedBuffer": 300,
						  "lastBalanceUpdate": "2026-06-03"
						}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.monthlyIncome").value(5000))
			.andExpect(jsonPath("$.currentBalance").value(1800.50))
			.andExpect(jsonPath("$.lastBalanceUpdate").value("2026-06-03"));

		mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.protectedBuffer").value(300));

		mockMvc
			.perform(post("/api/purchase-checks").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "amount": 42.90,
						  "verdict": "Safe",
						  "consequence": "This keeps you on track.",
						  "checkedAt": "2026-06-03T04:30:00Z"
						}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isString())
			.andExpect(jsonPath("$.amount").value(42.90))
			.andExpect(jsonPath("$.verdict").value("Safe"))
			.andExpect(jsonPath("$.checkedAt").value("2026-06-03T04:30:00Z"));

		mockMvc.perform(get("/api/purchase-checks").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].consequence").value("This keeps you on track."));
	}

	@Test
	void clearsCurrentUserProfileAndPurchaseChecks() throws Exception {
		String accessToken = authenticate("clear@spendly.com");

		mockMvc
			.perform(put("/api/profile").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "monthlyIncome": 5000,
						  "monthlyCommitments": 2200,
						  "currentBalance": 1800,
						  "protectedBuffer": 300,
						  "lastBalanceUpdate": "2026-06-03"
						}
						"""))
			.andExpect(status().isOk());

		mockMvc
			.perform(post("/api/purchase-checks").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "amount": 42.90,
						  "verdict": "Risky",
						  "consequence": "This makes the next few days tighter.",
						  "checkedAt": "2026-06-03T04:30:00Z"
						}
						"""))
			.andExpect(status().isOk());

		mockMvc.perform(delete("/api/me/data").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true));

		mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/purchase-checks").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void rejectsInvalidPurchaseVerdict() throws Exception {
		String accessToken = authenticate("verdict@spendly.com");

		mockMvc
			.perform(post("/api/purchase-checks").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "amount": 42.90,
						  "verdict": "Maybe",
						  "consequence": "Unknown.",
						  "checkedAt": "2026-06-03T04:30:00Z"
						}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Verdict must be Safe, Risky, or Not safe."));
	}

	private String authenticate(String email) throws Exception {
		ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

		mockMvc.perform(post("/api/auth/send-otp").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "%s"
				}
				""".formatted(email))).andExpect(status().isOk());

		verify(emailDeliveryService).sendOtp(eq(email), otpCaptor.capture());

		String authBody = mockMvc
			.perform(post("/api/auth/verify-otp").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "email": "%s",
					  "token": "%s"
					}
					""".formatted(email, otpCaptor.getValue())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readTree(authBody).get("accessToken").asText();
	}

}
