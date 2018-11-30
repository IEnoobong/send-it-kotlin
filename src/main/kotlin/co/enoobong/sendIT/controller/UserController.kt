package co.enoobong.sendIT.controller

import co.enoobong.sendIT.exception.UnauthorizedAccessException
import co.enoobong.sendIT.payload.BaseApiResponse
import co.enoobong.sendIT.security.CurrentUser
import co.enoobong.sendIT.security.UserPrincipal
import co.enoobong.sendIT.security.isUser
import co.enoobong.sendIT.service.ParcelService
import co.enoobong.sendIT.util.toHttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    "v1/users",
    produces = [MediaType.APPLICATION_JSON_UTF8_VALUE],
    consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class UserController(private val parcelService: ParcelService) {

    @GetMapping("{userId}/parcels")
    fun getParcelDeliveryOrder(@CurrentUser currentUser: UserPrincipal, @PathVariable("userId") userId: Long): ResponseEntity<BaseApiResponse> {
        val isUser = currentUser.isUser()
        if (isUser && currentUser.user.id != userId) {
            throw UnauthorizedAccessException("You can only view your parcel delivery orders")
        }
        val apiResponse = parcelService.getAllParcelDeliveryOrderForUser(userId)
        return ResponseEntity(apiResponse, apiResponse.status.toHttpStatus())
    }
}