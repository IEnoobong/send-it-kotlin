package co.enoobong.sendit.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException constructor(message: String) : RuntimeException(message) {
    constructor(
        resourceName: String,
        fieldName: String,
        fieldValue: Any?
    ) : this(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue))
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class AppException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedAccessException(message: String) : RuntimeException(message)

