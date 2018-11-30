package co.enoobong.sendIT.controller

import co.enoobong.sendIT.config.ControllerTest
import co.enoobong.sendIT.model.db.Address
import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.model.db.WeightMetric
import co.enoobong.sendIT.payload.ParcelDeliveryDTO
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.service.ParcelService
import co.enoobong.sendIT.utill.ADMIN_ID
import co.enoobong.sendIT.utill.ADMIN_TOKEN
import co.enoobong.sendIT.utill.USER_TOKEN
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@ControllerTest
@WebMvcTest(UserController::class)
class UserControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var parcelService: ParcelService

    @Test
    fun `get parcel deliveries by user id should return parcel delivery orders`() {
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
        given(parcelService.getAllParcelDeliveryOrderForUser(ADMIN_ID)).willReturn(apiResponse)

        mockMvc.perform(
            get("/api/v1/users/$ADMIN_ID/parcels")
                .header("Authorization", "Bearer $ADMIN_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("\$.status", `is`(httpStatus)))
            .andExpect(jsonPath("\$.data.[0]").isMap)
            .andExpect(jsonPath("\$.data.[0].id").value(parcelDeliveryDTO.parcelId))
    }

    @Test
    fun `attempt of user to get parcel delivery not belonging to user should be unauthorized`() {
        mockMvc.perform(
            get("/api/v1/users/3/parcels")
                .header("Authorization", "Bearer $USER_TOKEN")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("\$.status", `is`(HttpStatus.UNAUTHORIZED.value())))
            .andExpect(jsonPath("\$.error").isString)
    }
}