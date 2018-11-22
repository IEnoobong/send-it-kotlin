package co.enoobong.sendIT.repository


import co.enoobong.sendIT.model.db.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByUsernameOrEmail(username: String, email: String): Optional<User>
}