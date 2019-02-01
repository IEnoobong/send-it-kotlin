package co.enoobong.sendit.config

import co.enoobong.sendit.security.CustomUserDetailsService
import co.enoobong.sendit.security.JwtAuthenticationEntryPoint
import co.enoobong.sendit.security.JwtAuthenticationFilter
import co.enoobong.sendit.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val unauthorizedHandler: JwtAuthenticationEntryPoint,
    private val jwtTokenProvider: JwtTokenProvider
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.requiresChannel().requestMatchers(RequestMatcher { it.getHeader("X-Forwarded-Proto") != null })
            .requiresSecure().and()
            .cors()
            .and()
            .csrf()
            .disable()
            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedHandler)
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .enableStaticResourceAccess()
            .enableSwaggerAccess()
            .antMatchers("/api/**/auth/**")
            .permitAll()
            .antMatchers("/api/user/checkUsernameAvailability", "/api/user/checkEmailAvailability")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/api/users/**")
            .permitAll()
            .anyRequest()
            .authenticated()

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    private fun ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry.enableStaticResourceAccess(): ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry {
        return this.antMatchers(
            "/",
            "/favicon.ico",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.jpg",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js"
        )
            .permitAll()

    }

    private fun ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry.enableSwaggerAccess(): ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry {
        return this.antMatchers(
            "/csrf",
            "/v2/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            "/swagger-ui.html"
        )
            .permitAll()
    }
}