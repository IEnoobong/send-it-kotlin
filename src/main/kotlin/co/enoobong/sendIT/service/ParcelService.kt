package co.enoobong.sendIT.service

import co.enoobong.sendIT.exception.ResourceNotFoundException
import co.enoobong.sendIT.model.db.Parcel
import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.payload.ParcelDeliveryDTO
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.ParcelModifiedResponse
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.toParcel
import co.enoobong.sendIT.repository.ParcelRepository
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
}

@Service
class ParcelServiceImpl(private val parcelRepository: ParcelRepository) : ParcelService {

    override fun createParcel(parcelDeliveryRequest: ParcelDeliveryRequest): BaseApiResponse {
        val parcel = parcelDeliveryRequest.toParcel()

        val savedParcel = parcelRepository.save(parcel)

        return SuccessApiResponse(HttpStatus.CREATED.value(), listOf(ParcelModifiedResponse(savedParcel.id)))
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

    private fun cancelParcelDeliveryOrder(parcelId: Long): BaseApiResponse {
        val rowsUpdated =
            parcelRepository.updateParcelStatusWhereStatusIsNotDelivered(parcelId, ParcelStatus.CANCELLED)
        return if (rowsUpdated == 1) {
            val parcelModifiedResponse = ParcelModifiedResponse(parcelId, "order canceled")
            SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelModifiedResponse))
        } else {
            throw ResourceNotFoundException("Parcel with id $parcelId does not exist in undelivered state")
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
            val parcelModifiedResponse = ParcelModifiedResponse(parcelId, "order canceled")
            SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelModifiedResponse))
        } else {
            throw ResourceNotFoundException("Parcel with id $parcelId belonging to user with id $userId does not exist in undelivered state")
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


