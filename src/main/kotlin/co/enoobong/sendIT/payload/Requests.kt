package co.enoobong.sendIT.payload

import co.enoobong.sendIT.model.db.User
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class SignUpRequest(
    @field:NotBlank
    @field:Size(max = 40)
    @field:JsonProperty("firstname")
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 40)
    @field:JsonProperty("lastname")
    val lastName: String,
    @field:JsonProperty("othernames")
    val otherNames: String?,
    @field:Email
    @field:NotBlank
    @field:Size(max = 40)
    val email: String,
    @field:NotBlank
    @field:Size(min = 3, max = 15)
    val username: String,
    @field:NotBlank
    @field:Size(min = 5, max = 20)
    var password: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignUpRequest

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (otherNames != other.otherNames) return false
        if (email != other.email) return false
        if (username != other.username) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (otherNames?.hashCode() ?: 0)
        result = 31 * result + email.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}

fun SignUpRequest.toUser(): User {
    return with(this) {
        User(firstName, lastName, otherNames, username, email, password)
    }
}

class LoginRequest(
    @field:NotBlank
    @field:JsonProperty("usernameoremail")
    val userNameOrEmail: String,
    @field:NotBlank
    val password: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginRequest

        if (userNameOrEmail != other.userNameOrEmail) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userNameOrEmail.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}