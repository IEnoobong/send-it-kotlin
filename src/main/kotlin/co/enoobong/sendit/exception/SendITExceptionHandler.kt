package co.enoobong.sendit.exception

import co.enoobong.sendit.payload.ErrorApiResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class SendITExceptionHandler {

    private companion object {
        private val LOG = LoggerFactory.getLogger(SendITExceptionHandler::class.java)
    }

    @ExceptionHandler
    fun handleException(
        exception: Exception
    ): ResponseEntity<ErrorApiResponse> {
        LOG.error("Error Occurred", exception)
        return when (exception) {
            is MethodArgumentNotValidException -> {
                val errorMessageBuilder = StringBuilder()
                exception.bindingResult.fieldErrors.forEach {
                    errorMessageBuilder.append(it.field).append(" ").appendln(it.defaultMessage)
                }
                val badRequest = HttpStatus.BAD_REQUEST
                val errorApiResponse = ErrorApiResponse(badRequest.value(), errorMessageBuilder.trim().toString())
                ResponseEntity(errorApiResponse, badRequest)
            }
            else -> {
                val httpStatus = resolveAnnotatedResponseStatus(exception)

                val errorApiResponse = ErrorApiResponse(httpStatus.value(), exception.message)

                ResponseEntity(errorApiResponse, httpStatus)
            }
        }
    }

    private fun resolveAnnotatedResponseStatus(exception: Exception): HttpStatus {
        val annotation = findMergedAnnotation(exception.javaClass, ResponseStatus::class.java)
        return if (annotation?.value != null) {
            annotation.value
        } else {
            when (exception) {
                is AuthenticationException -> HttpStatus.UNAUTHORIZED
                is AccessDeniedException -> HttpStatus.UNAUTHORIZED
                is HttpMessageNotReadableException -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
        }
    }
}
