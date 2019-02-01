package co.enoobong.sendit.payload

import co.enoobong.sendit.model.db.Address
import co.enoobong.sendit.model.db.Parcel
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.model.db.User
import co.enoobong.sendit.model.db.WeightMetric
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank
    @field:Size(max = 40)
    @field:JsonProperty("first_name")
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 40)
    @field:JsonProperty("last_name")
    val lastName: String,
    @field:JsonProperty("other_names")
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

data class LoginRequest(
    @field:NotBlank
    @field:JsonProperty("username_or_email")
    val userNameOrEmail: String,
    @field:NotBlank
    val password: String
)

data class ParcelDeliveryRequest(
    @field:Positive
    val weight: Float,
    @field:NotNull
    val weightMetric: WeightMetric,
    @field:Valid
    val from: Address,
    @field:Valid
    val to: Address,
    @field:Valid
    @field:JsonProperty("current_location")
    val currentLocation: Address
)

data class ParcelStatusRequest(
    @field:Valid
    @field:NotNull
    @field:JsonProperty("new_status")
    val newStatus: ParcelStatus
)
fun ParcelDeliveryRequest.toParcel(): Parcel {
    return with(this) {
        Parcel(weight, weightMetric, ParcelStatus.PLACED, from, to, currentLocation)
    }
}