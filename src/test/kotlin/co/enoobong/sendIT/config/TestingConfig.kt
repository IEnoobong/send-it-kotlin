package co.enoobong.sendIT.config

import co.enoobong.sendIT.model.db.Role
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import co.enoobong.sendIT.repository.UserRepository
import co.enoobong.sendIT.utill.ADMIN_ID
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
        val userRole = Role(RoleName.ROLE_USER, 1)
        val adminRole = Role(RoleName.ROLE_ADMIN, 1)
        val user = User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", "yagathem", USER_ID)
        user.roles.add(userRole)
        val admin = User("Eno", "Ibanga", null, "ienoobong", "ibanga@yahoo.co", "yagathem", USER_ID)
        admin.roles.add(adminRole)
        whenever(userRepository.findById(USER_ID)).thenReturn(Optional.of(user))
        whenever(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin))
        return userRepository
    }
}