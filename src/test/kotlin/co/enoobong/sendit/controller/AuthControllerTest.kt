package co.enoobong.sendit.controller

import co.enoobong.sendit.config.ControllerTest
import co.enoobong.sendit.model.db.User
import co.enoobong.sendit.payload.ErrorApiResponse
import co.enoobong.sendit.payload.LoginRequest
import co.enoobong.sendit.payload.SignUpRequest
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.payload.UserResponse
import co.enoobong.sendit.payload.toUser
import co.enoobong.sendit.service.AuthService
import co.enoobong.sendit.service.toUserDTO
import co.enoobong.sendit.utill.ResponseBodyMatchers.Companion.responseBody
import co.enoobong.sendit.utill.USER_TOKEN
import co.enoobong.sendit.utill.toJsonString
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@ControllerTest
@WebMvcTest(value = [AuthController::class])
class AuthControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `sign up should create user account and return token + user details`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", "yagthem")
        val user = signUpRequest.toUser()
        user.createdAt = Instant.now()
        val signUpResponse = UserResponse(USER_TOKEN, user.toUserDTO())
        val apiResponse = SuccessApiResponse(
            HttpStatus.CREATED.value(),
            listOf(signUpResponse)
        )
        given(authService.signUpUser(signUpRequest)).willReturn(apiResponse)

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(signUpRequest.toJsonString())
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(header().exists("location"))
            .andExpect(responseBody().containsObjectAsSuccessApiResponseJson<UserResponse>(apiResponse))
    }

    @Test
    fun `sign up with already taken email or username should return bad request`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", "yagthem")
        given(authService.signUpUser(signUpRequest)).willReturn(
            ErrorApiResponse(
                HttpStatus.BAD_REQUEST.value(),
                "email or user name is taken"
            )
        )

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(signUpRequest.toJsonString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("\$.error").isString)
    }

    @Test
    fun `login with correct credentials should log in user`() {
        val loginRequest = LoginRequest("ibanga@yahoo.co", "yagathem ")
        val user = User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", "yagathem")
        user.createdAt = Instant.now()
        val loginResponse = UserResponse(USER_TOKEN, user.toUserDTO())
        val apiResponse = SuccessApiResponse(HttpStatus.OK.value(), listOf(loginResponse))
        given(authService.loginUser(loginRequest)).willReturn(apiResponse)

        mockMvc.perform {
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(loginRequest.toJsonString())
                .buildRequest(it)
        }.andExpect(status().isOk)
            .andExpect(responseBody().containsObjectAsSuccessApiResponseJson<UserResponse>(apiResponse))
    }
}