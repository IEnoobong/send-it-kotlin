package co.enoobong.sendit.controller

import co.enoobong.sendit.exception.UnauthorizedAccessException
import co.enoobong.sendit.payload.BaseApiResponse
import co.enoobong.sendit.security.CurrentUser
import co.enoobong.sendit.security.UserPrincipal
import co.enoobong.sendit.security.isUser
import co.enoobong.sendit.service.ParcelService
import co.enoobong.sendit.util.toHttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping(
    "v1/users",
    produces = [MediaType.APPLICATION_JSON_UTF8_VALUE],
    consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class UserController(private val parcelService: ParcelService) {

    @GetMapping("{userId}/parcels")
    fun getParcelDeliveryOrder(
        @ApiIgnore @CurrentUser currentUser: UserPrincipal,
        @PathVariable("userId") userId: Long
    ):
            ResponseEntity<BaseApiResponse> {
        val isUser = currentUser.isUser()
        if (isUser && currentUser.user.id != userId) {
            throw UnauthorizedAccessException("You can only view your parcel delivery orders")
        }
        val apiResponse = parcelService.getAllParcelDeliveryOrderForUser(userId)
        return ResponseEntity(apiResponse, apiResponse.status.toHttpStatus())
    }
}