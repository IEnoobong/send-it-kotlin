package co.enoobong.sendIT.service

import co.enoobong.sendIT.exception.AppException
import co.enoobong.sendIT.model.db.Role
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import co.enoobong.sendIT.payload.ErrorApiResponse
import co.enoobong.sendIT.payload.LoginRequest
import co.enoobong.sendIT.payload.SignUpRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.UserResponse
import co.enoobong.sendIT.payload.toUser
import co.enoobong.sendIT.repository.RoleRepository
import co.enoobong.sendIT.repository.UserRepository
import co.enoobong.sendIT.security.JwtTokenProvider
import co.enoobong.sendIT.security.UserPrincipal
import co.enoobong.sendIT.utill.ENCRYPTED_PASSWORD
import co.enoobong.sendIT.utill.TOKEN
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Optional

class AuthServiceImplTest {

    private val userRepository = mockk<UserRepository>()

    private val roleRepository = mockk<RoleRepository>()

    private val passwordEncoder = mockk<PasswordEncoder>()

    private val tokenProvider = mockk<JwtTokenProvider>()

    private val authenticationManager = mockk<AuthenticationManager>()

    private val authService =
        AuthServiceImpl(userRepository, roleRepository, passwordEncoder, tokenProvider, authenticationManager)

    @Test
    fun `sign up should create new user`() {
        val password = "yagathem"
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", password)

        every { passwordEncoder.encode(password) } returns ENCRYPTED_PASSWORD

        val role = Role(RoleName.ROLE_USER, 1)
        every { roleRepository.findRoleByName(RoleName.ROLE_USER) } returns Optional.of(role)

        val user = signUpRequest.toUser()
        user.createdAt = Instant.now()
        every { userRepository.save(any<User>()) } returns user

        every { tokenProvider.generateToken(user.id) } returns TOKEN

        val apiResponse = authService.signUpUser(signUpRequest)

        @Suppress("UNCHECKED_CAST")
        assertAll(
            { assertEquals(HttpStatus.CREATED.value(), apiResponse.status) },

            { assertTrue(apiResponse is SuccessApiResponse<*>) },

            {
                apiResponse as SuccessApiResponse<UserResponse>
                assertEquals(TOKEN, apiResponse.data[0].token)

            },

            {
                apiResponse as SuccessApiResponse<UserResponse>
                assertEquals(signUpRequest.firstName, apiResponse.data[0].user.firstName)
            }
        )

        verifyOrder {
            passwordEncoder.encode(password)
            roleRepository.findRoleByName(RoleName.ROLE_USER)
            userRepository.save(any<User>())
            tokenProvider.generateToken(user.id)
        }

    }


    @Test
    fun `sign up should throw AppException when user role not set`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", "yagathem")
        every { passwordEncoder.encode(signUpRequest.password) } returns ENCRYPTED_PASSWORD
        every { roleRepository.findRoleByName(RoleName.ROLE_USER) } returns Optional.empty()

        val exception = assertThrows<AppException> {
            authService.signUpUser(signUpRequest)
        }
        assertEquals("${RoleName.ROLE_USER} has not been set", exception.message)

    }

    @Test
    fun `sign up should return error response when data integrity is violated`() {
        val password = "yagathem"
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", password)

        every { passwordEncoder.encode(password) } returns ENCRYPTED_PASSWORD

        val role = Role(RoleName.ROLE_USER, 1)
        every { roleRepository.findRoleByName(RoleName.ROLE_USER) } returns Optional.of(role)

        every { userRepository.save(any<User>()) } throws DataIntegrityViolationException("")

        val apiResponse = authService.signUpUser(signUpRequest)

        assertAll(
            { assertEquals(HttpStatus.BAD_REQUEST.value(), apiResponse.status) },

            { assertTrue(apiResponse is ErrorApiResponse) },

            {
                apiResponse as ErrorApiResponse
                assertEquals("email or user name is taken", apiResponse.error)

            }
        )

        verifyOrder {
            passwordEncoder.encode(password)
            roleRepository.findRoleByName(RoleName.ROLE_USER)
            userRepository.save(any<User>())
        }
    }

    @Test
    fun `login should log in valid user`() {
        val loginRequest = LoginRequest("ibanga@yahoo.co", "yagathem ")

        val authentication = mockk<Authentication>()
        val role = Role(RoleName.ROLE_USER, 1)
        val user =
            User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", ENCRYPTED_PASSWORD, 1, mutableSetOf(role))
        user.createdAt = Instant.now()
        val userPrincipal = UserPrincipal.create(user)
        every { authentication.principal } returns userPrincipal

        every {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.userNameOrEmail,
                    loginRequest.password
                )
            )
        } returns authentication

        every { tokenProvider.generateToken(user.id) } returns TOKEN

        val apiResponse = authService.loginUser(loginRequest)

        @Suppress("UNCHECKED_CAST")
        assertAll(
            { assertEquals(HttpStatus.OK.value(), apiResponse.status) },

            { assertTrue(apiResponse is SuccessApiResponse<*>) },

            {
                apiResponse as SuccessApiResponse<UserResponse>
                assertEquals(TOKEN, apiResponse.data[0].token)

            },

            {
                apiResponse as SuccessApiResponse<UserResponse>
                assertEquals(loginRequest.userNameOrEmail, apiResponse.data[0].user.email)
            }
        )

        verifyOrder {
            authenticationManager.authenticate(any())
            authentication.principal
            tokenProvider.generateToken(user.id)
        }

    }

}