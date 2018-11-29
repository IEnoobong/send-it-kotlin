package co.enoobong.sendIT.service

import co.enoobong.sendIT.model.db.Address
import co.enoobong.sendIT.model.db.Parcel
import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.model.db.WeightMetric
import co.enoobong.sendIT.payload.ParcelCreatedResponse
import co.enoobong.sendIT.payload.ParcelDeliveryDTO
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.payload.toParcel
import co.enoobong.sendIT.repository.ParcelRepository
import co.enoobong.sendIT.utill.USER_ID
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.http.HttpStatus
import java.util.Optional

class ParcelServiceImplTest {

    private val parcelRepository = mock<ParcelRepository>()

    private val parcelService = ParcelServiceImpl(parcelRepository)

    @Test
    fun `create parcel should create parcel delivery`() {
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)
        val toParcel = parcelDeliveryRequest.toParcel()

        whenever(parcelRepository.save(toParcel)).thenReturn(toParcel)

        val createParcelResponse = parcelService.createParcel(parcelDeliveryRequest)

        createParcelResponse as SuccessApiResponse<*>
        val parcelCreatedResponse = createParcelResponse.data[0] as ParcelCreatedResponse
        assertAll(
            {
                assertEquals(HttpStatus.CREATED.value(), createParcelResponse.status)
            },
            {
                assertEquals(0, parcelCreatedResponse.parcelId)
            },
            {
                assertEquals("order created", parcelCreatedResponse.message)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).save(toParcel)
        }
    }

    @Test
    fun `get all parcel deliveries should return all parcel delivery orders`() {
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcel = Parcel(1f, WeightMetric.KG, ParcelStatus.TRANSITING, address, address, address)
        whenever(parcelRepository.findAll()).thenReturn(listOf(parcel))

        val allParcelDeliveryOrders = parcelService.getAllParcelDeliveryOrders()

        allParcelDeliveryOrders as SuccessApiResponse<*>
        val parcelDeliveryDTO = allParcelDeliveryOrders.data[0] as ParcelDeliveryDTO
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), allParcelDeliveryOrders.status)
            },
            {
                assertEquals(parcel.id, parcelDeliveryDTO.parcelId)
            },
            {
                assertEquals(address.displayableAddress(), parcelDeliveryDTO.currentLocation)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).findAll()
        }
    }

    @Test
    fun `get parcel delivery for a user should return user's parcel delivery order`() {
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelId = 1L
        val parcel = Parcel(1f, WeightMetric.KG, ParcelStatus.TRANSITING, address, address, address, parcelId)
        whenever(parcelRepository.findByIdAndCreatedBy(parcelId, USER_ID)).thenReturn(Optional.of(parcel))

        val orderForUser = parcelService.getParcelDeliveryOrderForUser(USER_ID, parcelId)

        orderForUser as SuccessApiResponse<*>
        val parcelDeliveryDTO = orderForUser.data[0] as ParcelDeliveryDTO
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), orderForUser.status)
            },
            {
                assertEquals(parcel.id, parcelDeliveryDTO.parcelId)
            },
            {
                assertEquals(address.displayableAddress(), parcelDeliveryDTO.currentLocation)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).findByIdAndCreatedBy(USER_ID, parcelId)
        }
    }

    @Test
    fun `get parcel delivery for should return parcel delivery order`() {
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelId = 1L
        val parcel = Parcel(1f, WeightMetric.KG, ParcelStatus.TRANSITING, address, address, address, parcelId)
        whenever(parcelRepository.findById(parcelId)).thenReturn(Optional.of(parcel))

        val parcelDelivery = parcelService.getParcelDeliveryOrder(parcelId)

        parcelDelivery as SuccessApiResponse<*>
        val parcelDeliveryDTO = parcelDelivery.data[0] as ParcelDeliveryDTO
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), parcelDelivery.status)
            },
            {
                assertEquals(parcel.id, parcelDeliveryDTO.parcelId)
            },
            {
                assertEquals(address.displayableAddress(), parcelDeliveryDTO.currentLocation)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).findById(parcelId)
        }
    }

    @Test
    fun `get all parcel delivery for user should return user's parcel delivery orders`() {
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelId = 1L
        val parcel = Parcel(1f, WeightMetric.KG, ParcelStatus.TRANSITING, address, address, address, parcelId)
        whenever(parcelRepository.findByCreatedBy(USER_ID)).thenReturn(listOf(parcel))

        val usersParcelDeliveries = parcelService.getAllParcelDeliveryOrderForUser(USER_ID)

        usersParcelDeliveries as SuccessApiResponse<*>
        val parcelDeliveryDTO = usersParcelDeliveries.data[0] as ParcelDeliveryDTO
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), usersParcelDeliveries.status)
            },
            {
                assertEquals(parcel.id, parcelDeliveryDTO.parcelId)
            },
            {
                assertEquals(address.displayableAddress(), parcelDeliveryDTO.currentLocation)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).findByCreatedBy(USER_ID)
        }
    }
}
