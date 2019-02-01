package co.enoobong.sendit.config

import co.enoobong.sendit.security.CustomUserDetailsService
import co.enoobong.sendit.security.JwtAuthenticationEntryPoint
import co.enoobong.sendit.security.JwtTokenProvider
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Import(value = [WebConfig::class, CustomUserDetailsService::class, TestingConfig::class, JwtAuthenticationEntryPoint::class, JwtTokenProvider::class])
annotation class ControllerTest