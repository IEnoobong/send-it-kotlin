package co.enoobong.sendit.controller

import co.enoobong.sendit.config.ControllerTest
import co.enoobong.sendit.model.db.Address
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.model.db.WeightMetric
import co.enoobong.sendit.payload.ErrorApiResponse
import co.enoobong.sendit.payload.ParcelDeliveryDTO
import co.enoobong.sendit.payload.ParcelDeliveryRequest
import co.enoobong.sendit.payload.ParcelModifiedResponse
import co.enoobong.sendit.payload.ParcelStatusRequest
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.security.JwtTokenProvider
import co.enoobong.sendit.service.ParcelService
import co.enoobong.sendit.utill.ADMIN_ID
import co.enoobong.sendit.utill.ResponseBodyMatchers
import co.enoobong.sendit.utill.USER_ID
import co.enoobong.sendit.utill.USER_TOKEN
import co.enoobong.sendit.utill.toJsonString
import com.nhaarman.mockito_kotlin.given
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@ControllerTest
@WebMvcTest(ParcelController::class)
class ParcelControllerTest(@Autowired private val mockMvc: MockMvc, @Autowired private val jwtTokenProvider: JwtTokenProvider) {

    @MockBean
    private lateinit var parcelService: ParcelService

    @Test
    fun `create parcel delivery order should create parcel delivery order`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        val parcelCreatedResponse = ParcelModifiedResponse(1, "order created")
        val apiResponse = SuccessApiResponse(HttpStatus.CREATED.value(), listOf(parcelCreatedResponse))
        given(parcelService.createParcel(parcelDeliveryRequest)).willReturn(apiResponse)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelModifiedResponse>(
                    apiResponse
                )
            )
    }

    @Test
    fun `attempt to create parcel delivery order with no authorization should be unauthorized`() {
        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        mockMvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create parcel delivery with invalid fields should respond with bad request`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("\$.error").isString)
    }

    @Test
    fun `create parcel delivery when not created should return bad request`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        val apiResponse =
            ErrorApiResponse(HttpStatus.BAD_REQUEST.value(), "Error occurred when trying to create parcel delivery")
        given(parcelService.createParcel(parcelDeliveryRequest)).willReturn(apiResponse)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(apiResponse.status))
            .andExpect(jsonPath("\$.error").isString)

    }

    @Test
    fun `get all parcel delivery orders should return the delivery orders`() {
        val adminToken = jwtTokenProvider.generateToken(ADMIN_ID)

        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryDTO = ParcelDeliveryDTO(
            1,
            1,
            10f,
            WeightMetric.KG,
            Instant.now(),
            Instant.now(),
            ParcelStatus.PLACED,
            address.displayableAddress(),
            address.displayableAddress(),
            address.displayableAddress()
        )

        val apiResponse =
            SuccessApiResponse(HttpStatus.OK.value(), listOf(parcelDeliveryDTO))
        given(parcelService.getAllParcelDeliveryOrders()).willReturn(apiResponse)

        mockMvc.perform(
            get("/api/v1/parcels")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelDeliveryDTO>(
                    apiResponse
                )
            )

    }

    @Test
    fun `get all parcel delivery orders with wrong role should be unauthorized`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        mockMvc.perform(
            get("/api/v1/parcels")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("\$.error").isString)
    }

    @Test
    fun `get all parcel delivery orders with expired token should be unauthorized`() {
        mockMvc.perform(
            get("/api/v1/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `get parcel delivery order for non-admin by id should return delivery orders`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val parcelId = 1L
        val httpStatus = HttpStatus.OK.value()
        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryDTO = ParcelDeliveryDTO(
            1,
            1,
            10f,
            WeightMetric.KG,
            Instant.now(),
            Instant.now(),
            ParcelStatus.PLACED,
            address.displayableAddress(),
            address.displayableAddress(),
            address.displayableAddress()
        )
        val apiResponse = SuccessApiResponse(httpStatus, listOf(parcelDeliveryDTO))
        given(parcelService.getParcelDeliveryOrderForUser(USER_ID, parcelId)).willReturn(apiResponse)

        mockMvc.perform(
            get("/api/v1/parcels/$parcelId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelDeliveryDTO>(
                    apiResponse
                )
            )
    }

    @Test
    fun `get parcel delivery order for admin by id should return delivery orders`() {
        val adminToken = jwtTokenProvider.generateToken(ADMIN_ID)

        val parcelId = 1L
        val httpStatus = HttpStatus.OK.value()
        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryDTO = ParcelDeliveryDTO(
            1,
            1,
            10f,
            WeightMetric.KG,
            Instant.now(),
            Instant.now(),
            ParcelStatus.PLACED,
            address.displayableAddress(),
            address.displayableAddress(),
            address.displayableAddress()
        )
        val apiResponse = SuccessApiResponse(httpStatus, listOf(parcelDeliveryDTO))
        given(parcelService.getParcelDeliveryOrder(parcelId)).willReturn(apiResponse)

        mockMvc.perform(
            get("/api/v1/parcels/$parcelId")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelDeliveryDTO>(
                    apiResponse
                )
            )
    }

    @Test
    fun `cancel parcel delivery order should cancel parcel delivery order`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val parcelId = 1L
        val httpStatus = HttpStatus.OK.value()
        val parcelModifiedResponse = ParcelModifiedResponse(parcelId, "order cancelled")
        val apiResponse = SuccessApiResponse(httpStatus, listOf(parcelModifiedResponse))
        given(parcelService.cancelParcelDeliveryOrder(true, USER_ID, 1)).willReturn(apiResponse)

        mockMvc.perform(
            patch("/api/v1/parcels/$parcelId/cancel")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelModifiedResponse>(
                    apiResponse
                )
            )
    }

    @Test
    fun `change parcel destination should change it's destination`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)

        val parcelId = 1L
        val httpStatus = HttpStatus.OK.value()
        val newDestination = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelModifiedResponse = ParcelModifiedResponse(parcelId, "order cancelled")
        val apiResponse = SuccessApiResponse(httpStatus, listOf(parcelModifiedResponse))
        given(parcelService.changeParcelDirection(true, USER_ID, 1, newDestination)).willReturn(apiResponse)

        mockMvc.perform(
            patch("/api/v1/parcels/$parcelId/destination")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newDestination.toJsonString())
        ).andExpect(status().isOk)
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelModifiedResponse>(
                    apiResponse
                )
            )
    }

    @Test
    fun `change parcel status should change it's status`() {
        val adminToken = jwtTokenProvider.generateToken(ADMIN_ID)

        val parcelId = 1L
        val httpStatus = HttpStatus.OK.value()
        val newStatus = ParcelStatusRequest(ParcelStatus.TRANSITING)
        val parcelModifiedResponse = ParcelModifiedResponse(parcelId, "Parcel location updated")
        val apiResponse = SuccessApiResponse(httpStatus, listOf(parcelModifiedResponse))
        given(parcelService.changeParcelStatus(parcelId, ParcelStatus.TRANSITING)).willReturn(apiResponse)

        mockMvc.perform(
            patch("/api/v1/parcels/$parcelId/status")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newStatus.toJsonString())
        ).andExpect(status().isOk)
            .andExpect(
                ResponseBodyMatchers.responseBody().containsObjectAsSuccessApiResponseJson<ParcelModifiedResponse>(
                    apiResponse
                )
            )
    }

}