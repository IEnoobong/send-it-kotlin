package co.enoobong.sendIT.controller

import co.enoobong.sendIT.config.ControllerTest
import co.enoobong.sendIT.model.db.User
import co.enoobong.sendIT.payload.ErrorApiResponse
import co.enoobong.sendIT.payload.LoginRequest
import co.enoobong.sendIT.payload.SignUpRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.UserResponse
import co.enoobong.sendIT.payload.toUser
import co.enoobong.sendIT.service.AuthService
import co.enoobong.sendIT.service.toUserDTO
import co.enoobong.sendIT.utill.USER_TOKEN
import co.enoobong.sendIT.utill.toJsonString
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
        given(authService.signUpUser(signUpRequest)).willReturn(
            SuccessApiResponse(
                HttpStatus.CREATED.value(),
                listOf(signUpResponse)
            )
        )

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(signUpRequest.toJsonString())
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.CREATED.value()))
            .andExpect(jsonPath("\$.data.[0].token").isString)
            .andExpect(jsonPath("\$.data.[0].user").isMap)
            .andReturn()
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
        given(authService.loginUser(loginRequest)).willReturn(
            SuccessApiResponse(
                HttpStatus.OK.value(),
                listOf(loginResponse)
            )
        )

        mockMvc.perform {
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(loginRequest.toJsonString())
                .buildRequest(it)
        }.andExpect(status().isOk)
            .andExpect(jsonPath("\$.status").value(HttpStatus.OK.value()))
            .andExpect(jsonPath("\$.data.[0].token").isString)
            .andExpect(jsonPath("\$.data.[0].user").isMap)

    }
}