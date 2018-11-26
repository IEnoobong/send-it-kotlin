package co.enoobong.sendIT.config

import co.enoobong.sendIT.security.CustomUserDetailsService
import co.enoobong.sendIT.security.JwtAuthenticationEntryPoint
import co.enoobong.sendIT.security.JwtTokenProvider
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Import(value = [WebConfig::class, CustomUserDetailsService::class, TestingConfig::class, JwtAuthenticationEntryPoint::class, JwtTokenProvider::class])
annotation class ControllerTest