package co.enoobong.sendIT.exception

import co.enoobong.sendIT.payload.ErrorApiResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class SendITExceptionHandler {

    private companion object {
        private val LOG = LoggerFactory.getLogger(SendITExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun handleException(
        exception: Exception,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ErrorApiResponse> {
        val httpStatus = resolveAnnotatedResponseStatus(exception)

        LOG.error("Error Occurred", exception)

        val errorApiResponse = ErrorApiResponse(httpStatus.value(), exception.message)

        return ResponseEntity(errorApiResponse, httpStatus)

    }

    fun resolveAnnotatedResponseStatus(exception: Exception): HttpStatus {
        val annotation = findMergedAnnotation(exception.javaClass, ResponseStatus::class.java)
        return if (annotation?.value != null) {
            annotation.value
        } else {
            when (exception) {
                is AuthenticationException -> HttpStatus.UNAUTHORIZED
                is HttpMessageNotReadableException -> HttpStatus.BAD_REQUEST
                is MethodArgumentNotValidException -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
        }
    }
}
