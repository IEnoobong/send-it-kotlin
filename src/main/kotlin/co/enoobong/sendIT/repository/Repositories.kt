package co.enoobong.sendIT.repository


import co.enoobong.sendIT.model.db.Parcel
import co.enoobong.sendIT.model.db.Role
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByUsernameOrEmail(username: String, email: String): Optional<User>
}

@Repository
interface RoleRepository : CrudRepository<Role, Long> {

    fun findRoleByName(name: RoleName): Optional<Role>

}

@Repository
interface ParcelRepository : JpaRepository<Parcel, Long> {

    fun findByCreatedBy(userId: Long): List<Parcel>

    fun findByIdAndCreatedBy(parcelId: Long, userId: Long): Optional<Parcel>
}
