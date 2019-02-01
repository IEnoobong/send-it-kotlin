package co.enoobong.sendit.service

import co.enoobong.sendit.exception.ResourceNotFoundException
import co.enoobong.sendit.model.db.Address
import co.enoobong.sendit.model.db.Parcel
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.model.db.WeightMetric
import co.enoobong.sendit.payload.ParcelDeliveryDTO
import co.enoobong.sendit.payload.ParcelDeliveryRequest
import co.enoobong.sendit.payload.ParcelModifiedResponse
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.payload.toParcel
import co.enoobong.sendit.repository.ParcelRepository
import co.enoobong.sendit.utill.USER_ID
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
        val parcelCreatedResponse = createParcelResponse.data[0] as ParcelModifiedResponse
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

    @Test
    fun `cancel delivery order as user should cancel delivery order`() {
        val parcelId = 1L
        val parcelStatus = ParcelStatus.CANCELLED
        whenever(
            parcelRepository.updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
                parcelId,
                USER_ID,
                parcelStatus
            )
        ).thenReturn(1)

        val cancelParcelDeliveryOrder = parcelService.cancelParcelDeliveryOrder(true, USER_ID, parcelId)

        cancelParcelDeliveryOrder as SuccessApiResponse<*>
        val parcelModifiedResponse = cancelParcelDeliveryOrder.data[0] as ParcelModifiedResponse
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), cancelParcelDeliveryOrder.status)
            },
            {
                assertEquals(parcelId, parcelModifiedResponse.parcelId)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
                parcelId,
                USER_ID,
                parcelStatus
            )
        }
    }

    @Test
    fun `cancel delivery order should cancel delivery order`() {
        val parcelId = 1L
        val parcelStatus = ParcelStatus.CANCELLED
        whenever(parcelRepository.updateParcelStatusWhereStatusIsNotDelivered(parcelId, parcelStatus)).thenReturn(1)

        val cancelParcelDeliveryOrder = parcelService.cancelParcelDeliveryOrder(false, USER_ID, parcelId)

        cancelParcelDeliveryOrder as SuccessApiResponse<*>
        val parcelModifiedResponse = cancelParcelDeliveryOrder.data[0] as ParcelModifiedResponse
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), cancelParcelDeliveryOrder.status)
            },
            {
                assertEquals(parcelId, parcelModifiedResponse.parcelId)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).updateParcelStatusWhereStatusIsNotDelivered(parcelId, parcelStatus)
        }
    }

    @Test
    fun `cancel delivery order as user when delivery order already delivered or not found should throw exception`() {
        val parcelId = 1L
        val parcelStatus = ParcelStatus.CANCELLED
        whenever(
            parcelRepository.updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
                parcelId,
                USER_ID,
                parcelStatus
            )
        ).thenReturn(0)

        val exception = assertThrows<ResourceNotFoundException> {
            parcelService.cancelParcelDeliveryOrder(true, USER_ID, parcelId)
        }

        assertEquals(
            "Parcel with id $parcelId belonging to user with id $USER_ID does not exist in undelivered state",
            exception.message
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
                parcelId,
                USER_ID,
                parcelStatus
            )
        }
    }

    @Test
    fun `cancel delivery order when delivery order already delivered or not found should throw exception`() {
        val parcelId = 1L
        val parcelStatus = ParcelStatus.CANCELLED
        whenever(parcelRepository.updateParcelStatusWhereStatusIsNotDelivered(parcelId, parcelStatus)).thenReturn(0)

        val exception = assertThrows<ResourceNotFoundException> {
            parcelService.cancelParcelDeliveryOrder(false, USER_ID, parcelId)
        }

        assertEquals("Parcel with id $parcelId does not exist in undelivered state", exception.message)

        inOrder(parcelRepository) {
            verify(parcelRepository).updateParcelStatusWhereStatusIsNotDelivered(parcelId, parcelStatus)
        }
    }

    private companion object {
        @JvmStatic
        private fun parcelDestinationParams(): List<Arguments> {
            return listOf(
                Arguments.of(true),
                Arguments.of(false)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("parcelDestinationParams")
    fun `change parcel delivery destination should change it's destination`(isUser: Boolean) {
        val parcelId = 1L
        val newDestination = Address(1, "Udemba", "Saka", "Nice", "France")
        setupDeliveryRepo(newDestination, isUser, parcelId, 1)

        val changeParcelOrderDirection = parcelService.changeParcelDirection(isUser, USER_ID, parcelId, newDestination)

        changeParcelOrderDirection as SuccessApiResponse<*>
        val parcelModifiedResponse = changeParcelOrderDirection.data[0] as ParcelModifiedResponse
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), changeParcelOrderDirection.status)
            },
            {
                assertEquals(parcelId, parcelModifiedResponse.parcelId)
            }
        )

        verifyDeliveryDestinationRepos(isUser, parcelId, newDestination)
    }

    private fun verifyDeliveryDestinationRepos(isUser: Boolean, parcelId: Long, newDestination: Address) {
        val (streetNumber, streetName, city, state, country, zipCode) = newDestination
        inOrder(parcelRepository) {
            if (isUser) {
                verify(parcelRepository).changeUserUndeliveredParcelDestination(
                    parcelId,
                    USER_ID,
                    streetNumber, streetName, city, state, country, zipCode
                )
            } else {
                verify(parcelRepository).changeUndeliveredParcelDestination(
                    parcelId,
                    streetNumber, streetName, city, state, country, zipCode
                )
            }
        }
    }

    private fun setupDeliveryRepo(
        newDestination: Address,
        isUser: Boolean,
        parcelId: Long, valueToReturn: Int
    ) {
        val (streetNumber, streetName, city, state, country, zipCode) = newDestination
        if (isUser) {
            whenever(
                parcelRepository.changeUserUndeliveredParcelDestination(
                    parcelId,
                    USER_ID,
                    streetNumber, streetName, city, state, country, zipCode
                )
            ).thenReturn(valueToReturn)
        } else {
            whenever(
                parcelRepository.changeUndeliveredParcelDestination(
                    parcelId,
                    streetNumber, streetName, city, state, country, zipCode
                )
            ).thenReturn(valueToReturn)
        }
    }

    @ParameterizedTest
    @MethodSource("parcelDestinationParams")
    fun `change parcel delivery destination when delivery order already delivered or not found should throw exception`(
        isUser: Boolean
    ) {
        val parcelId = 1L
        val newDestination = Address(1, "Udemba", "Saka", "Nice", "France")
        setupDeliveryRepo(newDestination, isUser, parcelId, 0)

        val exception = assertThrows<ResourceNotFoundException> {
            parcelService.changeParcelDirection(isUser, USER_ID, parcelId, newDestination)
        }

        if (isUser) {
            assertEquals(
                "Couldn't change destination of parcel with id $parcelId for user with id $USER_ID",
                exception.message
            )
        } else {
            assertEquals(
                "Couldn't change destination of parcel with id $parcelId",
                exception.message
            )
        }

        verifyDeliveryDestinationRepos(isUser, parcelId, newDestination)
    }

    @Test
    fun `change parcel status should change it's status`() {
        val parcelId = 1L
        val parcelStatus = ParcelStatus.TRANSITING
        whenever(parcelRepository.updateParcelStatusById(parcelId, parcelStatus)).thenReturn(1)

        val changeParcelStatus = parcelService.changeParcelStatus(parcelId, parcelStatus)


        changeParcelStatus as SuccessApiResponse<*>
        val parcelModifiedResponse = changeParcelStatus.data[0] as ParcelModifiedResponse
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), changeParcelStatus.status)
            },
            {
                assertEquals(parcelId, parcelModifiedResponse.parcelId)
            },
            {
                assertEquals(parcelStatus.name, parcelModifiedResponse.status)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).updateParcelStatusById(parcelId, parcelStatus)
        }
    }

    @Test
    fun `change parcel current location should change it's current location`() {
        val parcelId = 1L
        val currentLocation = Address(1, "Udemba", "Saka", "Nice", "France")
        val (streetNumber, streetName, city, state, country, zipCode) = currentLocation
        whenever(
            parcelRepository.updateCurrentLocationById(
                parcelId,
                streetNumber,
                streetName,
                city,
                state,
                country,
                zipCode
            )
        ).thenReturn(1)

        val changeParcelLocation = parcelService.changeParcelCurrentLocation(parcelId, currentLocation)


        changeParcelLocation as SuccessApiResponse<*>
        val parcelModifiedResponse = changeParcelLocation.data[0] as ParcelModifiedResponse
        assertAll(
            {
                assertEquals(HttpStatus.OK.value(), changeParcelLocation.status)
            },
            {
                assertEquals(parcelId, parcelModifiedResponse.parcelId)
            },
            {
                assertEquals(currentLocation.displayableAddress(), parcelModifiedResponse.currentLocation)
            }
        )

        inOrder(parcelRepository) {
            verify(parcelRepository).updateCurrentLocationById(
                parcelId,
                streetNumber,
                streetName,
                city,
                state,
                country,
                zipCode
            )
        }
    }
}
