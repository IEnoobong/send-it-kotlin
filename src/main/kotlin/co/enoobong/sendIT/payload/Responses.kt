package co.enoobong.sendIT.payload

import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.model.db.WeightMetric
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

abstract class BaseApiResponse(val status: Int)

class SuccessApiResponse<T>(status: Int, val data: List<T>) : BaseApiResponse(status)

class ErrorApiResponse(status: Int, val error: String?) : BaseApiResponse(status)

class UserResponse(val token: String, val user: UserDTO)

data class UserDTO(
    val id: Long,
    @get:JsonProperty("first_name")
    val firstName: String,
    @get:JsonProperty("last_name")
    val lastName: String,
    @field:JsonProperty("other_names")
    val otherNames: String?,
    val username: String,
    val email: String,
    val registered: Instant,
    val isAdmin: Boolean
)

class ParcelModifiedResponse(
    @field:JsonProperty("id") val parcelId: Long,
    val message: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val to: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val status: String? = null,
    @field:JsonProperty("current_location")
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val currentLocation: String? = null
)

class ParcelDeliveryDTO(
    @field:JsonProperty("id")
    val parcelId: Long,
    @field:JsonProperty("placed_by")
    val placedBy: Long,
    val weight: Float,
    @field:JsonProperty("weight_metric")
    val weightMetric: WeightMetric,
    @field:JsonProperty("sent_on")
    val sentOn: Instant?,
    @field:JsonProperty("delivered_on")
    val deliveredOn: Instant?,
    val status: ParcelStatus,
    val from: String,
    val to: String,
    @field:JsonProperty("current_location")
    val currentLocation: String
)
