package co.enoobong.sendit.service

import co.enoobong.sendit.exception.ResourceNotFoundException
import co.enoobong.sendit.model.db.Address
import co.enoobong.sendit.model.db.Parcel
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.payload.BaseApiResponse
import co.enoobong.sendit.payload.ParcelDeliveryDTO
import co.enoobong.sendit.payload.ParcelDeliveryRequest
import co.enoobong.sendit.payload.ParcelModifiedResponse
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.payload.toParcel
import co.enoobong.sendit.repository.ParcelRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.ArrayList
import javax.transaction.Transactional

interface ParcelService {

    fun createParcel(parcelDeliveryRequest: ParcelDeliveryRequest): BaseApiResponse

    fun getAllParcelDeliveryOrders(): BaseApiResponse

    fun getParcelDeliveryOrder(parcelId: Long): BaseApiResponse

    fun getAllParcelDeliveryOrderForUser(userId: Long): BaseApiResponse

    fun getParcelDeliveryOrderForUser(userId: Long, parcelId: Long): BaseApiResponse

    fun cancelParcelDeliveryOrder(isUser: Boolean, userId: Long, parcelId: Long): BaseApiResponse

    fun changeParcelDirection(
        isUser: Boolean,
        userId: Long,
        parcelId: Long,
        newDestination: Address
    ): BaseApiResponse

    fun changeParcelStatus(parcelId: Long, newStatus: ParcelStatus): BaseApiResponse

    fun changeParcelCurrentLocation(parcelId: Long, currentLocation: Address): BaseApiResponse
}

@Service
class ParcelServiceImpl(private val parcelRepository: ParcelRepository) : ParcelService {

    private companion object {
        private const val PARCEL_DESTINATION_MESSAGE = "Parcel destination updated"
        private const val PARCEL_CREATED_MESSAGE = "order created"
        private const val PARCEL_CANCELLED_MESSAGE = "order cancelled"
        private const val PARCEL_STATUS_MESSAGE = "Parcel status updated"
        private const val PARCEL_LOCATION_MESSAGE = "Parcel location updated"
    }

    override fun createParcel(parcelDeliveryRequest: ParcelDeliveryRequest): BaseApiResponse {
        val parcel = parcelDeliveryRequest.toParcel()

        val savedParcel = parcelRepository.save(parcel)

        return SuccessApiResponse(
            HttpStatus.CREATED.value(),
            listOf(ParcelModifiedResponse(savedParcel.id, PARCEL_CREATED_MESSAGE))
        )
    }

    override fun getAllParcelDeliveryOrders(): BaseApiResponse {
        val allParcelDeliveryOrders = parcelRepository.findAll()
        val size = allParcelDeliveryOrders.size

        val parcelDeliveries = allParcelDeliveryOrders.mapTo(ArrayList(size)) { it.toParcelDeliveryDTO() }
        return SuccessApiResponse(HttpStatus.OK.value(), parcelDeliveries)
    }

    override fun getParcelDeliveryOrderForUser(userId: Long, parcelId: Long): BaseApiResponse {
        val parcelDeliveryOrder = parcelRepository.findByIdAndCreatedBy(parcelId, userId).orElseThrow {
            ResourceNotFoundException("Parcel", "parcelId", parcelId)
        }
        return SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelDeliveryOrder.toParcelDeliveryDTO()))
    }

    override fun getParcelDeliveryOrder(parcelId: Long): BaseApiResponse {
        val parcelDeliveryOrder = parcelRepository.findById(parcelId).orElseThrow {
            ResourceNotFoundException("Parcel", "parcelId", parcelId)
        }
        return SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelDeliveryOrder.toParcelDeliveryDTO()))
    }

    override fun getAllParcelDeliveryOrderForUser(userId: Long): BaseApiResponse {
        val userParcelDeliveries = parcelRepository.findByCreatedBy(userId)
        val size = userParcelDeliveries.size

        val parcelDeliveryDTOs = userParcelDeliveries.mapTo(ArrayList(size)) { it.toParcelDeliveryDTO() }
        return SuccessApiResponse(HttpStatus.OK.value(), parcelDeliveryDTOs)
    }

    @Transactional
    override fun cancelParcelDeliveryOrder(isUser: Boolean, userId: Long, parcelId: Long): BaseApiResponse {
        return if (isUser) {
            cancelUserParcelDeliveryOrder(userId, parcelId)
        } else {
            cancelParcelDeliveryOrder(parcelId)
        }
    }

    private fun cancelUserParcelDeliveryOrder(
        userId: Long,
        parcelId: Long
    ): BaseApiResponse {
        val rowsUpdated =
            parcelRepository.updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
                userId,
                parcelId,
                ParcelStatus.CANCELLED
            )
        return if (rowsUpdated == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_CANCELLED_MESSAGE)
        } else {
            throw ResourceNotFoundException("Parcel with id $parcelId belonging to user with id $userId does not exist in undelivered state")
        }
    }

    private fun cancelParcelDeliveryOrder(parcelId: Long): BaseApiResponse {
        val rowsUpdated =
            parcelRepository.updateParcelStatusWhereStatusIsNotDelivered(parcelId, ParcelStatus.CANCELLED)
        return if (rowsUpdated == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_CANCELLED_MESSAGE)
        } else {
            throw ResourceNotFoundException("Parcel with id $parcelId does not exist in undelivered state")
        }
    }

    private fun parcelModifiedSuccessResponse(
        parcelId: Long,
        message: String,
        newDestination: Address? = null,
        newStatus: ParcelStatus? = null,
        newLocation: Address? = null
    ): SuccessApiResponse<ParcelModifiedResponse> {
        val parcelModifiedResponse = ParcelModifiedResponse(
            parcelId,
            message,
            newDestination?.displayableAddress(),
            newStatus?.name,
            newLocation?.displayableAddress()
        )
        return SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelModifiedResponse))
    }

    @Transactional
    override fun changeParcelDirection(
        isUser: Boolean,
        userId: Long,
        parcelId: Long,
        newDestination: Address
    ): BaseApiResponse {
        return if (isUser) {
            changeUserParcelDirection(userId, parcelId, newDestination)
        } else {
            changeParcelDirection(parcelId, newDestination)
        }
    }

    private fun changeParcelDirection(
        parcelId: Long,
        newDestination: Address
    ): SuccessApiResponse<ParcelModifiedResponse> {
        val (streetNumber, streetName, city, state, country, zipCode) = newDestination
        val rowsAffected =
            parcelRepository.changeUndeliveredParcelDestination(
                parcelId,
                streetNumber,
                streetName,
                city,
                state,
                country,
                zipCode
            )
        return if (rowsAffected == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_DESTINATION_MESSAGE, newDestination)
        } else {
            throw ResourceNotFoundException("Couldn't change destination of parcel with id $parcelId")
        }
    }

    private fun changeUserParcelDirection(
        userId: Long,
        parcelId: Long,
        newDestination: Address
    ): SuccessApiResponse<ParcelModifiedResponse> {
        val (streetNumber, streetName, city, state, country, zipCode) = newDestination
        val rowsAffected =
            parcelRepository.changeUserUndeliveredParcelDestination(
                userId,
                parcelId,
                streetNumber,
                streetName,
                city,
                state,
                country,
                zipCode
            )
        return if (rowsAffected == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_DESTINATION_MESSAGE, newDestination)
        } else {
            throw ResourceNotFoundException("Couldn't change destination of parcel with id $parcelId for user with id $userId")
        }
    }

    @Transactional
    override fun changeParcelStatus(parcelId: Long, newStatus: ParcelStatus): BaseApiResponse {
        val rowsAffected = parcelRepository.updateParcelStatusById(parcelId, newStatus)
        return if (rowsAffected == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_STATUS_MESSAGE, newStatus = newStatus)
        } else {
            throw ResourceNotFoundException("Couldn't change status of parcel with id $parcelId")
        }
    }

    @Transactional
    override fun changeParcelCurrentLocation(parcelId: Long, currentLocation: Address): BaseApiResponse {
        val (streetNumber, streetName, city, state, country, zipCode) = currentLocation
        val rowsAffected = parcelRepository.updateCurrentLocationById(
            parcelId,
            streetNumber,
            streetName,
            city,
            state,
            country,
            zipCode
        )
        return if (rowsAffected == 1) {
            parcelModifiedSuccessResponse(parcelId, PARCEL_LOCATION_MESSAGE, newLocation = currentLocation)
        } else {
            throw ResourceNotFoundException("Couldn't change current location of parcel with id $parcelId")
        }
    }
}

fun Parcel.toParcelDeliveryDTO(): ParcelDeliveryDTO {
    return with(this) {
        ParcelDeliveryDTO(
            id,
            createdBy,
            weight,
            weightMetric,
            sentOn,
            deliveredOn,
            parcelStatus,
            from.displayableAddress(),
            to.displayableAddress(),
            currentLocation.displayableAddress()
        )
    }
}


