package co.enoobong.sendIT.security

import co.enoobong.sendIT.payload.ErrorApiResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    private companion object {
        private val LOG = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        LOG.error("Responding with unauthorized error for path ${request.servletPath}. Message", authException)

        val unauthorizedCode = HttpServletResponse.SC_UNAUTHORIZED

        response.status = unauthorizedCode
        response.addHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)

        val errorApiResponse = ErrorApiResponse(unauthorizedCode, authException.message)

        jacksonObjectMapper().writeValue(response.outputStream, errorApiResponse)
        response.flushBuffer()
    }
}