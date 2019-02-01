package co.enoobong.sendit.config

import co.enoobong.sendit.model.db.Role
import co.enoobong.sendit.model.db.RoleName
import co.enoobong.sendit.model.db.User
import co.enoobong.sendit.repository.UserRepository
import co.enoobong.sendit.utill.ADMIN_ID
import co.enoobong.sendit.utill.USER_ID
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