package co.enoobong.sendIT.controller

import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.payload.ParcelDeliveryRequest
import co.enoobong.sendIT.payload.ParcelModifiedResponse
import co.enoobong.sendIT.payload.SuccessApiResponse
import co.enoobong.sendIT.security.CurrentUser
import co.enoobong.sendIT.security.UserPrincipal
import co.enoobong.sendIT.service.ParcelService
import co.enoobong.sendIT.util.toHttpStatus
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
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
            val parcelId = response.data[0] as ParcelModifiedResponse
            val location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/parcels/{parcelId}")
                .buildAndExpand(parcelId.parcelId).toUri()
            ResponseEntity.created(location).body(response)
        } else {
            ResponseEntity(response, response.status.toHttpStatus())
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllParcelDeliveryOrders(): ResponseEntity<BaseApiResponse> {
        val response = parcelService.getAllParcelDeliveryOrders()

        return ResponseEntity(response, response.status.toHttpStatus())
    }

    @GetMapping("{parcelId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getParcelDeliveryOrder(@CurrentUser currentUser: UserPrincipal, @PathVariable("parcelId") parcelId: Long): ResponseEntity<BaseApiResponse> {
        val notAdmin = !currentUser.user.roles.map { it.name }.contains(RoleName.ROLE_ADMIN)
        if (notAdmin) {
            val response = parcelService.getParcelDeliveryOrderForUser(currentUser.user.id, parcelId)
            return ResponseEntity(response, response.status.toHttpStatus())
        }
        val response = parcelService.getParcelDeliveryOrder(parcelId)

        return ResponseEntity(response, response.status.toHttpStatus())
    }

    @PatchMapping("{parcelId}/cancel")
    fun cancelParcelDeliveryOrder(@CurrentUser currentUser: UserPrincipal, @PathVariable("parcelId") parcelId: Long): ResponseEntity<BaseApiResponse> {
        val isUser = currentUser.user.roles.map { it.name }.contains(RoleName.ROLE_USER)

        val response = parcelService.cancelParcelDeliveryOrder(isUser, currentUser.user.id, parcelId)
        return ResponseEntity(response, response.status.toHttpStatus())
    }
}