package co.enoobong.sendIT.exception

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionHandler {

    private companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @ExceptionHandler
    fun handleException(
        exception: Exception,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ExceptionRepresentation> {
        val timeStamp = Instant.now()
        val httpStatus = resolveAnnotatedResponseStatus(exception)

        LOG.error("Error Occurred", exception)

        val exceptionRepresentation =
            ExceptionRepresentation(
                timeStamp,
                httpStatus.value(),
                httpStatus.reasonPhrase,
                exception.message,
                httpRequest.servletPath
            )

        return ResponseEntity(exceptionRepresentation, httpStatus)

    }

    fun resolveAnnotatedResponseStatus(exception: Exception): HttpStatus {
        if (exception is AuthenticationException) return HttpStatus.UNAUTHORIZED
        val annotation = findMergedAnnotation(exception.javaClass, ResponseStatus::class.java)
        return annotation?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    }
}

class ExceptionRepresentation(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String
)