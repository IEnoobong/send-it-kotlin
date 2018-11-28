package co.enoobong.sendIT.service

import co.enoobong.sendIT.model.db.Parcel
import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.payload.ErrorApiResponse
import co.enoobong.sendIT.payload.ParcelCreatedResponse
import co.enoobong.sendIT.payload.ParcelDeliveryDTO
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.toParcel
import co.enoobong.sendIT.repository.ParcelRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.ArrayList

interface ParcelService {

    fun createParcel(parcelDeliveryRequest: ParcelDeliveryRequest): BaseApiResponse

    fun getAllParcelDeliveryOrders(): BaseApiResponse
}

@Service
class ParcelServiceImpl(private val parcelRepository: ParcelRepository) : ParcelService {

    private companion object {
        private val LOG = LoggerFactory.getLogger(ParcelServiceImpl::class.java)
    }

    override fun createParcel(parcelDeliveryRequest: ParcelDeliveryRequest): BaseApiResponse {
        val parcel = parcelDeliveryRequest.toParcel()
        return try {
            val savedParcel = parcelRepository.save(parcel)

            SuccessApiResponse(HttpStatus.CREATED.value(), listOf(ParcelCreatedResponse(savedParcel.id)))
        } catch (ex: Exception) {
            LOG.error("Error occurred when trying to save parcel", ex)
            ErrorApiResponse(HttpStatus.BAD_REQUEST.value(), "Error occurred when trying to create parcel delivery")
        }
    }

    override fun getAllParcelDeliveryOrders(): BaseApiResponse {
        val allParcelDeliveryOrders = parcelRepository.findAll()
        val size = allParcelDeliveryOrders.size

        val parcelDeliveries = allParcelDeliveryOrders.mapTo(ArrayList(size)) { it.toParcelDeliveryDTO() }
        return SuccessApiResponse(HttpStatus.OK.value(), parcelDeliveries)
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


