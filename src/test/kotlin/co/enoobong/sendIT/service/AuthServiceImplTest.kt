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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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

    private companion object {
        private const val password = "yagathem"
    }

    private val userRepository = mock<UserRepository> {

    }

    private val roleRepository = mock<RoleRepository>()

    private val passwordEncoder = mock<PasswordEncoder> {
        on { encode(password) } doReturn (ENCRYPTED_PASSWORD)
    }

    private val tokenProvider = mock<JwtTokenProvider>()

    private val authenticationManager = mock<AuthenticationManager>()

    private val authService =
        AuthServiceImpl(userRepository, roleRepository, passwordEncoder, tokenProvider, authenticationManager)

    @Test
    fun `sign up should create new user`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", password)

        val role = Role(RoleName.ROLE_USER, 1)
        whenever(roleRepository.findRoleByName(RoleName.ROLE_USER)) doReturn Optional.of(role)

        val user = signUpRequest.toUser()
        user.createdAt = Instant.now()
        whenever(userRepository.save(any<User>())) doReturn user

        whenever(tokenProvider.generateToken(user.id)) doReturn TOKEN

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

        inOrder(passwordEncoder, roleRepository, userRepository, tokenProvider) {
            verify(passwordEncoder).encode(password)

            verify(roleRepository).findRoleByName(RoleName.ROLE_USER)

            verify(userRepository).save(any<User>())

            verify(tokenProvider).generateToken(user.id)
        }

    }


    @Test
    fun `sign up should throw AppException when user role not set`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", "yagathem")
        whenever(roleRepository.findRoleByName(RoleName.ROLE_USER)) doReturn Optional.empty()

        val exception = assertThrows<AppException> {
            authService.signUpUser(signUpRequest)
        }
        assertEquals("${RoleName.ROLE_USER} has not been set", exception.message)
    }

    @Test
    fun `sign up should return error response when data integrity is violated`() {
        val signUpRequest = SignUpRequest("Eno", "Ibanga", null, "ibanga@yahoo.co", "ienoobong", password)

        val role = Role(RoleName.ROLE_USER, 1)
        whenever(roleRepository.findRoleByName(RoleName.ROLE_USER)) doReturn Optional.of(role)

        whenever(userRepository.save(any<User>())) doThrow DataIntegrityViolationException("Constraint Violation")

        val apiResponse = authService.signUpUser(signUpRequest)

        assertAll(
            { assertEquals(HttpStatus.BAD_REQUEST.value(), apiResponse.status) },

            { assertTrue(apiResponse is ErrorApiResponse) },

            {
                apiResponse as ErrorApiResponse
                assertEquals("email or user name is taken", apiResponse.error)

            }
        )

        inOrder(passwordEncoder, roleRepository, userRepository) {
            verify(passwordEncoder).encode(password)
            verify(roleRepository).findRoleByName(RoleName.ROLE_USER)
            verify(userRepository).save(any<User>())
        }
    }

    @Test
    fun `login should log in valid user`() {
        val loginRequest = LoginRequest("ibanga@yahoo.co", "yagathem ")

        val role = Role(RoleName.ROLE_USER, 1)
        val user =
            User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", ENCRYPTED_PASSWORD, 1, mutableSetOf(role))
        user.createdAt = Instant.now()
        val userPrincipal = UserPrincipal.create(user)
        val authentication = mock<Authentication> {
            on { principal } doReturn userPrincipal
        }

        whenever(
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.userNameOrEmail,
                    loginRequest.password
                )
            )
        ) doReturn authentication

        whenever(tokenProvider.generateToken(user.id)) doReturn TOKEN

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

        inOrder(authenticationManager, authentication, tokenProvider) {
            verify(authenticationManager).authenticate(any())
            verify(authentication).principal
            verify(tokenProvider).generateToken(user.id)
        }

    }

}