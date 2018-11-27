package co.enoobong.sendIT.config

import co.enoobong.sendIT.model.db.Role
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import co.enoobong.sendIT.repository.UserRepository
import co.enoobong.sendIT.utill.USER_ID
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.Optional

@Configuration
@Profile("test")
class TestingConfig {

    @Bean
    fun userRepository(): UserRepository {
        val userRepository = mock<UserRepository>()
        val role = Role(RoleName.ROLE_USER, 1)
        val user = User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", "yagathem", USER_ID)
        user.roles.add(role)
        whenever(userRepository.findById(USER_ID)).thenReturn(Optional.of(user))
        return userRepository
    }
}