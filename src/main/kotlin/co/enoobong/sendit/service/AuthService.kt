package co.enoobong.sendit.service

import co.enoobong.sendit.exception.AppException
import co.enoobong.sendit.model.db.RoleName
import co.enoobong.sendit.model.db.User
import co.enoobong.sendit.payload.BaseApiResponse
import co.enoobong.sendit.payload.ErrorApiResponse
import co.enoobong.sendit.payload.LoginRequest
import co.enoobong.sendit.payload.SignUpRequest
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.payload.UserDTO
import co.enoobong.sendit.payload.UserResponse
import co.enoobong.sendit.payload.toUser
import co.enoobong.sendit.repository.RoleRepository
import co.enoobong.sendit.repository.UserRepository
import co.enoobong.sendit.security.JwtTokenProvider
import co.enoobong.sendit.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface AuthService {

    fun signUpUser(signUpRequest: SignUpRequest): BaseApiResponse

    fun loginUser(loginRequest: LoginRequest): BaseApiResponse
}

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) : AuthService {

    private companion object {
        private val LOG = LoggerFactory.getLogger(AuthServiceImpl::class.java)
    }

    override fun signUpUser(signUpRequest: SignUpRequest): BaseApiResponse {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)
        signUpRequest.password = encodedPassword
        val user = signUpRequest.toUser()

        val userRole = roleRepository.findRoleByName(RoleName.ROLE_USER)
            .orElseThrow { AppException("${RoleName.ROLE_USER} has not been set") }
        user.roles.add(userRole)

        return try {
            val savedUser = userRepository.save(user)
            val signUpResponse = UserResponse(tokenProvider.generateToken(savedUser.id), savedUser.toUserDTO())
            SuccessApiResponse(HttpStatus.CREATED.value(), listOf(signUpResponse))
        } catch (ex: DataIntegrityViolationException) {
            LOG.error("An error occurred", ex)
            ErrorApiResponse(HttpStatus.BAD_REQUEST.value(), "email or user name is taken")
        }
    }

    override fun loginUser(loginRequest: LoginRequest): BaseApiResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.userNameOrEmail,
                loginRequest.password
            )
        )

        SecurityContextHolder.getContext().authentication = authentication

        val userPrincipal = authentication.principal as UserPrincipal

        val jwt = tokenProvider.generateToken(userPrincipal.user.id)
        val loginResponse = UserResponse(jwt, userPrincipal.user.toUserDTO())
        return SuccessApiResponse(HttpStatus.OK.value(), listOf(loginResponse))
    }

}

fun User.toUserDTO(): UserDTO {
    return with(this) {
        UserDTO(
            id,
            firstName,
            lastName,
            otherNames,
            username,
            email,
            createdAt,
            this.roles.map { it.name }.contains(RoleName.ROLE_ADMIN)
        )
    }
}
