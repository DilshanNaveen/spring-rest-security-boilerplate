package com.springrestsecurityboilerplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springrestsecurityboilerplate.password.PasswordChange;
import com.springrestsecurityboilerplate.user.AppUser;
import com.springrestsecurityboilerplate.user.UserRepository;
import com.springrestsecurityboilerplate.user.UserService;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.jsonwebtoken.lang.Assert;

@SpringBootTest
// @WebAppConfiguration
// @AutoConfigureMockMvc
@ContextConfiguration
public class Tests {

	private static MockMvc mockMvc;

	@Autowired
	WebApplicationContext wac;

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private static JacksonTester<AppUser> jsonTester;

	private static JacksonTester<PasswordChange> jsonTesterPswChange;

	AppUser registerUser;

	AppUser loginUser = new AppUser();

	AppUser tempUser;

	AppUser resendTokenUser;
	
	AppUser resetPasswordUser;

	static String bearerToken;

	static String resetPasswordToken;

	String lastToken;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	// @BeforeClass
	// public void setup() {
	// System.out.println("Before executed");
	//// this.mockMvc =
	// MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
	//// JacksonTester.initFields(this, objectMapper);
	//
	// }

	@Given("^configurations /config$")
	public void configg() throws Throwable {

		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
		JacksonTester.initFields(this, objectMapper);
		System.out.println("setup executed");

	}

	@When("^username password and email are \"([^\"]*)\" AND \"(.*?)\" AND \"(.*?)\"$")
	public void userRegister(String username, String password, String email) throws Throwable {
		registerUser = new AppUser();
		registerUser.setUsername(username);
		registerUser.setPassword(password);
		registerUser.setEmail(email);

	}

	@Then("^visitor calls /register$")
	public void performRegister() throws Throwable {

		final String personDTOJson = jsonTester.write(registerUser).getJson();

		mockMvc.perform(post("/register").content(personDTOJson).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isCreated()).andDo(MockMvcResultHandlers.print());

	}

	@When("^attempt to register with registered email but different username \"([^\"]*)\" AND \"(.*?)\" AND \"(.*?)\"$")
	public void userRegisterWithRegisteredEmail(String username, String password, String email) throws Throwable {
		registerUser = new AppUser();
		registerUser.setUsername(username);
		registerUser.setPassword(password);
		registerUser.setEmail(email);

	}

	@And("^make sure that there is already an account with that email \"(.*?)\"$")
	public void checkingForRegisteredEmail(String email) throws Throwable {

		tempUser = new AppUser();
		tempUser = userRepository.findByEmail(email);
		Assert.notNull(tempUser, "Should not be Null");

	}

	@Then("^make sure register is failure with HTTP Response Conflict and there is no user with different username \"(.*?)\"$")
	public void performRegisterWithRegisteredEmail(String username) throws Throwable {

		final String personDTOJson = jsonTester.write(registerUser).getJson();

		mockMvc.perform(post("/register").content(personDTOJson).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isConflict()).andDo(MockMvcResultHandlers.print());

		tempUser = new AppUser();
		tempUser = userRepository.findByUsername(username);
		Assert.isNull(tempUser, "Should be Null");

	}

	@When("^attempt to register with registered username \"([^\"]*)\" AND \"(.*?)\" AND \"(.*?)\"$")
	public void userRegisterWithRegisteredUsername(String username, String password, String email) throws Throwable {
		registerUser = new AppUser();
		registerUser.setUsername(username);
		registerUser.setPassword(password);
		registerUser.setEmail(email);

	}

	@And("^make sure that there is already an account with that username \"(.*?)\"$")
	public void checkingForRegisteredUsername(String username) throws Throwable {

		tempUser = new AppUser();
		tempUser = userRepository.findByUsername(username);
		Assert.notNull(tempUser, "Should not be Null");

	}

	@Then("^make sure register is failure with HTTP Response Conflict and there is no user with different email \"(.*?)\"$")
	public void performRegisterWithRegisteredUsername(String email) throws Throwable {

		final String personDTOJson = jsonTester.write(registerUser).getJson();

		mockMvc.perform(post("/register").content(personDTOJson).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isConflict()).andDo(MockMvcResultHandlers.print());

		tempUser = new AppUser();
		tempUser = userRepository.findByEmail(email);
		Assert.isNull(tempUser, "Should be Null");

	}

	@When("^Resend Token with unregistered user \"([^\"]*)\"$")
	public void resendUnregisteredUserToken(String email) throws Throwable {
		MvcResult mvcResult;

		resendTokenUser = new AppUser();

		mvcResult = mockMvc.perform(get("/resend/{email}", email)).andExpect(status().is4xxClientError()).andReturn();

		resendTokenUser = userRepository.findByEmail(email);

		// Assert.isNull(tempUser, "Should be Null");

	}

	@Then("^Make sure that user is null$")
	public void userIsNull() throws Throwable {

		Assert.isNull(resendTokenUser, "Should be Null");

	}

	@When("^Resend Token \"([^\"]*)\"$")
	public void resendToken(String email) throws Throwable {
		MvcResult mvcResult;

		resendTokenUser = new AppUser();
		mvcResult = mockMvc.perform(get("/resend/{email}", email)).andExpect(status().isOk()).andReturn();
		resendTokenUser = userRepository.findByEmail(email);

		lastToken = resendTokenUser.getToken().getToken();
		System.out.println("NEW TOKEN IS " + lastToken);

	}

	@And("^Make sure that user is not null$")
	public void userIsNotNull() throws Throwable {

		Assert.notNull(resendTokenUser, "Should not be Null");
	}

	@Then("^Make sure that user is not active$")
	public void userIsNotActive() throws Throwable {

		assertEquals(resendTokenUser.getIsActive(), false);
	}

	@When("^Confirm Token$")
	public void confirmToken() throws Throwable {
		MvcResult mvcResult;

		mvcResult = mockMvc.perform(get("/confirm/{email}", lastToken)).andExpect(status().isOk()).andReturn();
	}

	@Then("^Make sure that user is active \"([^\"]*)\"$")
	public void userIsActive(String email) throws Throwable {
		resendTokenUser = new AppUser();
		resendTokenUser = userRepository.findByEmail(email);
		assertEquals(resendTokenUser.getIsActive(), true);
	}

	@When("^Resend Token for already confirmed user \"([^\"]*)\"$")
	public void resendForAlreadyConfirmed(String email) throws Throwable {
		MvcResult mvcResult;

		resendTokenUser = new AppUser();

		mvcResult = mockMvc.perform(get("/resend/{email}", email)).andExpect(status().is4xxClientError()).andReturn();

		resendTokenUser = userRepository.findByEmail(email);

		// Assert.notNull(tempUser, "should not be null");
		// assertEquals(tempUser.getIsActive(), true);

	}

	@When("^Reset password by email \"([^\"]*)\"$")
	public void resetPasswordByEmail(String email) throws Throwable {

		MvcResult mvcResult;

		resetPasswordUser = new AppUser();
		mvcResult = mockMvc.perform(get("/resetpassword/{email}", email)).andExpect(status().isOk()).andReturn();
		resetPasswordUser = userRepository.findByEmail(email);
		resetPasswordToken = resetPasswordUser.getPasswordResetToken().getToken();
		System.out.println("Test Reset PasswordToken = " + resetPasswordToken);
	}

	@Then("^Reset password parameters with \"([^\"]*)\" AND \"([^\"]*)\"$")
	public void resetPassword(String password1, String password2) throws Throwable {
		MvcResult mvcResult;
		PasswordChange pswChange = new PasswordChange();
		pswChange.setPasswordOne(password1);
		pswChange.setPasswordTwo(password2);

		final String passwordChangeDTOJson = jsonTesterPswChange.write(pswChange).getJson();

		mvcResult = mockMvc.perform(post("/resetpasswordform/{token}", resetPasswordToken)
				.content(passwordChangeDTOJson).contentType(APPLICATION_JSON_UTF8)).andExpect(status().isOk()).

				andDo(MockMvcResultHandlers.print()).andReturn();

	}

	@When("^Successful login username and password are \"([^\"]*)\" AND \"(.*?)\"$")
	public void userLogin(String username, String password) throws Throwable {
		MvcResult mvcResult;
		loginUser.setUsername(username);
		loginUser.setPassword(password);
		final String personDTOJson = jsonTester.write(loginUser).getJson();

		mvcResult = mockMvc.perform(post("/login").content(personDTOJson).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk()).
				// andExpect(jsonPath("$.username",is("Destan"))).
				andDo(MockMvcResultHandlers.print()).andReturn();

		bearerToken = mvcResult.getResponse().getHeader("Authorization");

		System.out.println("Value is =" + bearerToken);

	}

	@When("^Login with bad creds username and password are \"([^\"]*)\" AND \"(.*?)\"$")
	public void userLoginWithBadCreds(String username, String password) throws Throwable {

		loginUser.setUsername(username);
		loginUser.setPassword(password);
		final String personDTOJson = jsonTester.write(loginUser).getJson();

		mockMvc.perform(post("/login").content(personDTOJson).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError()).
				// andExpect(jsonPath("$.username",is("Destan"))).
				andDo(MockMvcResultHandlers.print());

	}

	@Then("^Access with token /test$") // authentication
	public void displayBearerToken() throws Exception {
		mockMvc.perform(get("/test").header("authorization", bearerToken)).andExpect(status().isOk());
	}

	@Then("^Access without token /test$")
	public void the_client_issues_POST_hello2() throws Throwable {

		mockMvc.perform(get("/test")).andExpect(status().is4xxClientError());
		System.out.println("testauth");

	}

}
