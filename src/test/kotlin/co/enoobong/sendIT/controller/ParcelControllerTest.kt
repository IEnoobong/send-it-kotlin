package co.enoobong.sendIT.controller

import co.enoobong.sendIT.config.ControllerTest
import co.enoobong.sendIT.model.db.Address
import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.model.db.WeightMetric
import co.enoobong.sendIT.payload.ErrorApiResponse
import co.enoobong.sendIT.payload.ParcelCreatedResponse
import co.enoobong.sendIT.payload.ParcelDeliveryDTO
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.service.ParcelService
import co.enoobong.sendIT.utill.ADMIN_TOKEN
import co.enoobong.sendIT.utill.USER_ID
import co.enoobong.sendIT.utill.USER_TOKEN
import co.enoobong.sendIT.utill.toJsonString
import com.nhaarman.mockito_kotlin.given
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@ControllerTest
@WebMvcTest(ParcelController::class)
class ParcelControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var parcelService: ParcelService

    @Test
    fun `create parcel delivery order should create parcel delivery order`() {
        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        val parcelCreatedResponse = ParcelCreatedResponse(1)
        val apiResponse = SuccessApiResponse(HttpStatus.CREATED.value(), listOf(parcelCreatedResponse))
        given(parcelService.createParcel(parcelDeliveryRequest)).willReturn(apiResponse)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.CREATED.value()))
            .andExpect(jsonPath("\$.data.[0].id").value(parcelCreatedResponse.parcelId))
            .andExpect(jsonPath("\$.data.[0].message").value(parcelCreatedResponse.message))
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
        val address = Address(0, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("\$.error").isString)
    }

    @Test
    fun `create parcel delivery when not created should return bad request`() {
        val address = Address(1, "Udemba", "Saka", "Nice", "France")
        val parcelDeliveryRequest = ParcelDeliveryRequest(1f, WeightMetric.KG, address, address, address)

        val apiResponse =
            ErrorApiResponse(HttpStatus.BAD_REQUEST.value(), "Error occurred when trying to create parcel delivery")
        given(parcelService.createParcel(parcelDeliveryRequest)).willReturn(apiResponse)

        mockMvc.perform(
            post("/api/v1/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(parcelDeliveryRequest.toJsonString())
        ).andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(apiResponse.status))
            .andExpect(jsonPath("\$.error").isString)

    }

    @Test
    fun `get all parcel delivery orders should return the delivery orders`() {
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
                .header("Authorization", "Bearer $ADMIN_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.OK.value()))
            .andExpect(jsonPath("\$.data.[0]").isMap)

    }

    @Test
    fun `get all parcel delivery orders with wrong role should be unauthorized`() {
        mockMvc.perform(
            get("/api/v1/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("\$.status").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("\$.error").isString)

    }

    @Test
    fun `get parcel delivery order for non-admin by id should return delivery orders`() {
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
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("\$.status", `is`(httpStatus)))
            .andExpect(jsonPath("\$.data.[0]").isMap)
            .andExpect(jsonPath("\$.data.[0].id").value(parcelId))
    }

    @Test
    fun `get parcel delivery order for admin by id should return delivery orders`() {
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
                .header("Authorization", "Bearer $ADMIN_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("\$.status", `is`(httpStatus)))
            .andExpect(jsonPath("\$.data.[0]").isMap)
            .andExpect(jsonPath("\$.data.[0].id").value(parcelId))
    }

}