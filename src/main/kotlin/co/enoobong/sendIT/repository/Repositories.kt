package co.enoobong.sendIT.repository


import co.enoobong.sendIT.model.db.Parcel
import co.enoobong.sendIT.model.db.ParcelStatus
import co.enoobong.sendIT.model.db.Role
import co.enoobong.sendIT.model.db.RoleName
import co.enoobong.sendIT.model.db.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
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

    @Modifying
    @Query("UPDATE Parcel SET parcelStatus =:newStatus WHERE parcelStatus <> co.enoobong.sendIT.model.db.ParcelStatus.DELIVERED AND id = :parcelId AND created_by = :userId")
    fun updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
        @Param("userId") userId: Long, @Param("parcelId") parcelId: Long, @Param(
            "newStatus"
        ) newStatus: ParcelStatus
    ): Int

    @Modifying
    @Query("UPDATE Parcel SET parcelStatus =:newStatus WHERE parcelStatus <> co.enoobong.sendIT.model.db.ParcelStatus.DELIVERED AND id = :parcelId")
    fun updateParcelStatusWhereStatusIsNotDelivered(@Param("parcelId") parcelId: Long, @Param("newStatus") newStatus: ParcelStatus): Int
}
