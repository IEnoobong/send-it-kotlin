package co.enoobong.sendIT.service

import co.enoobong.sendIT.exception.AppException
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.payload.ErrorApiResponse
import co.enoobong.sendIT.payload.LoginRequest
import co.enoobong.sendIT.payload.SignUpRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.UserDTO
import co.enoobong.sendIT.payload.UserResponse
import co.enoobong.sendIT.payload.toUser
import co.enoobong.sendIT.repository.RoleRepository
import co.enoobong.sendIT.repository.UserRepository
import co.enoobong.sendIT.security.JwtTokenProvider
import co.enoobong.sendIT.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider,
    val authenticationManager: AuthenticationManager
) {

    private companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    fun signUpUser(signUpRequest: SignUpRequest): BaseApiResponse {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)
        println("Encoded $encodedPassword")
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

    fun loginUser(loginRequest: LoginRequest): BaseApiResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.userNameOrEmail,
                loginRequest.password
            )
        )

        SecurityContextHolder.getContext().authentication = authentication

        val userPrincipal = authentication.principal as UserPrincipal

        val jwt = tokenProvider.generateToken(userPrincipal.user.id)
        val signUpResponse = UserResponse(jwt, userPrincipal.user.toUserDTO())
        return SuccessApiResponse(HttpStatus.OK.value(), listOf(signUpResponse))
    }

}

private fun User.toUserDTO(): UserDTO {
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
