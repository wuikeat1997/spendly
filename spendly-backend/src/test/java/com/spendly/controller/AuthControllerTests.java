package com.spendly.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	@Test
	void sendsOtpVerifiesOtpAndReturnsCurrentUser() throws Exception {
		String email = "User@Spendly.com";
		ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

		mockMvc.perform(post("/api/auth/send-otp").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "User@Spendly.com",
				  "reason": "initial"
				}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true))
			.andExpect(jsonPath("$.requestCount").value(1))
			.andExpect(jsonPath("$.requestLimit").value(2));

		verify(emailDeliveryService).sendOtp(eq("user@spendly.com"), otpCaptor.capture());
		String otp = otpCaptor.getValue();
		assertThat(otp).matches("^[0-9]{6}$");

		String authBody = mockMvc
			.perform(post("/api/auth/verify-otp").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "email": "%s",
					  "token": "%s"
					}
					""".formatted(email, otp)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.expiresIn").value(3600))
			.andExpect(jsonPath("$.refreshToken").isString())
			.andExpect(jsonPath("$.refreshExpiresIn").value(2592000))
			.andExpect(jsonPath("$.user.refNo").value(startsWith("USR")))
			.andExpect(jsonPath("$.user.email").doesNotExist())
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode auth = objectMapper.readTree(authBody);
		String accessToken = auth.get("accessToken").asText();

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.refNo").value(startsWith("USR")))
			.andExpect(jsonPath("$.email").doesNotExist());
	}

	@Test
	void refreshRotatesSession() throws Exception {
		ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

		mockMvc.perform(post("/api/auth/send-otp").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "refresh@spendly.com"
				}
				""")).andExpect(status().isOk());

		verify(emailDeliveryService).sendOtp(eq("refresh@spendly.com"), otpCaptor.capture());

		String authBody = mockMvc
			.perform(post("/api/auth/verify-otp").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "email": "refresh@spendly.com",
					  "token": "%s"
					}
					""".formatted(otpCaptor.getValue())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String refreshToken = objectMapper.readTree(authBody).get("refreshToken").asText();

		String refreshedBody = mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(refreshToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.refNo").value(startsWith("USR")))
			.andExpect(jsonPath("$.user.email").doesNotExist())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String rotatedRefreshToken = objectMapper.readTree(refreshedBody).get("refreshToken").asText();
		assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

		mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "refreshToken": "%s"
				}
				""".formatted(refreshToken))).andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsOtpThatIsNotSixDigits() throws Exception {
		mockMvc.perform(post("/api/auth/verify-otp").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "user@spendly.com",
				  "token": "abc123"
				}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("OTP must be exactly 6 digits."));
	}

}
