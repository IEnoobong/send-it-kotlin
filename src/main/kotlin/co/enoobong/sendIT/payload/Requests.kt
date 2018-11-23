package co.enoobong.sendIT.payload

import co.enoobong.sendIT.model.db.User
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class SignUpRequest(
    @field:NotBlank
    @field:Size(max = 40)
    @JsonProperty("firstname")
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 40)
    @JsonProperty("lastname")
    val lastName: String,
    @JsonProperty("othernames")
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
)

fun SignUpRequest.toUser(): User {
    return with(this) {
        User(firstName, lastName, otherNames, username, email, password)
    }
}

class LoginRequest(
    @field:NotBlank
    @JsonProperty("usernameoremail")
    val userNameOrEmail: String,
    @field:NotBlank
    val password: String
)