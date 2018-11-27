package co.enoobong.sendIT.controller

import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.payload.ParcelCreatedResponse
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.service.ParcelService
import co.enoobong.sendIT.util.toHttpStatus
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping(
    "v1/parcels", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE],
    consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ParcelController(private val parcelService: ParcelService) {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createParcelDelivery(@Valid @RequestBody parcelDeliveryRequest: ParcelDeliveryRequest): ResponseEntity<BaseApiResponse> {
        val response = parcelService.createParcel(parcelDeliveryRequest)
        return if (response.status == HttpStatus.CREATED.value()) {
            response as SuccessApiResponse<*>
            val parcelId = response.data[0] as ParcelCreatedResponse
            val location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/parcels/{parcelId}")
                .buildAndExpand(parcelId.parcelId).toUri()
            ResponseEntity.created(location).body(response)
        } else {
            ResponseEntity(response, response.status.toHttpStatus())
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun getAllParcelDeliveryOrders(): ResponseEntity<BaseApiResponse> {
        val response = parcelService.getAllParcelDeliveryOrders()

        return ResponseEntity(response, response.status.toHttpStatus())
    }
}