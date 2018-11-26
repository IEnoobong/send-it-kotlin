package co.enoobong.sendIT.config

import co.enoobong.sendIT.repository.UserRepository
import com.nhaarman.mockito_kotlin.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestingConfig {

    @Bean
    fun userRepository(): UserRepository {
        return mock()
    }
}