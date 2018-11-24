package co.enoobong.sendIT.payload

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

abstract class BaseApiResponse(val status: Int)

class SuccessApiResponse<T>(status: Int, val data: List<T>) : BaseApiResponse(status)

class ErrorApiResponse(status: Int, val error: String?) : BaseApiResponse(status)

class UserResponse(val token: String, val user: UserDTO)

class UserDTO(
    val id: Long,
    @get:JsonProperty("firstname")
    val firstName: String,
    @get:JsonProperty("lastname")
    val lastName: String,
    @field:JsonProperty("othernames")
    val otherNames: String?,
    val username: String,
    val email: String,
    val registered: Instant,
    val isAdmin: Boolean
)
