package co.enoobong.sendit.controller

import co.enoobong.sendit.config.ControllerTest
import co.enoobong.sendit.model.db.Address
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.model.db.WeightMetric
import co.enoobong.sendit.payload.ParcelDeliveryDTO
import co.enoobong.sendit.payload.SuccessApiResponse
import co.enoobong.sendit.security.JwtTokenProvider
import co.enoobong.sendit.service.ParcelService
import co.enoobong.sendit.utill.ADMIN_ID
import co.enoobong.sendit.utill.USER_ID
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
class UserControllerTest(@Autowired private val mockMvc: MockMvc, @Autowired private val jwtTokenProvider: JwtTokenProvider) {

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
        val adminToken = jwtTokenProvider.generateToken(ADMIN_ID)
        given(parcelService.getAllParcelDeliveryOrderForUser(ADMIN_ID)).willReturn(apiResponse)

        mockMvc.perform(
            get("/api/v1/users/$ADMIN_ID/parcels")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("\$.status", `is`(httpStatus)))
            .andExpect(jsonPath("\$.data.[0]").isMap)
            .andExpect(jsonPath("\$.data.[0].id").value(parcelDeliveryDTO.parcelId))
    }

    @Test
    fun `attempt of user to get parcel delivery not belonging to user should be unauthorized`() {
        val userToken = jwtTokenProvider.generateToken(USER_ID)
        mockMvc.perform(
            get("/api/v1/users/3/parcels")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("\$.status", `is`(HttpStatus.UNAUTHORIZED.value())))
            .andExpect(jsonPath("\$.error").isString)
    }
}