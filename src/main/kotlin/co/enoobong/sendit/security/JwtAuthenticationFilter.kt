package co.enoobong.sendit.security

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private companion object {
        private val LOG = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (!jwt.isNullOrBlank() && tokenProvider.validateToken(jwt)) {
                val userId = tokenProvider.getUserIdFromJWT(jwt)

                val userDetails = customUserDetailsService.loadUserById(userId)
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            LOG.error("Could not set user authentication in security context", ex)
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        val bearer = "Bearer "

        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith(bearer)) {
            bearerToken.substring(bearer.length, bearerToken.length)
        } else null
    }

}
