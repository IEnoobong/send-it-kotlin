package co.enoobong.sendit.repository


import co.enoobong.sendit.model.db.Parcel
import co.enoobong.sendit.model.db.ParcelStatus
import co.enoobong.sendit.model.db.Role
import co.enoobong.sendit.model.db.RoleName
import co.enoobong.sendit.model.db.User
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
    @Query("UPDATE Parcel SET parcelStatus =:newStatus WHERE parcelStatus <> co.enoobong.sendit.model.db.ParcelStatus.DELIVERED AND id = :parcelId AND created_by = :userId")
    fun updateParcelStatusWhereOwnerIsAndStatusIsNotDelivered(
        @Param("userId") userId: Long, @Param("parcelId") parcelId: Long, @Param(
            "newStatus"
        ) newStatus: ParcelStatus
    ): Int

    @Modifying
    @Query("UPDATE Parcel SET parcelStatus =:newStatus WHERE parcelStatus <> co.enoobong.sendit.model.db.ParcelStatus.DELIVERED AND id = :parcelId")
    fun updateParcelStatusWhereStatusIsNotDelivered(@Param("parcelId") parcelId: Long, @Param("newStatus") newStatus: ParcelStatus): Int

    @Modifying
    @Query(
        "UPDATE Parcel set " +
                "to.streetNumber =:streetNumber, to.streetName =:streetName, to.city = :city, to.state = :state, to.country = :country, to.zipCode = :zipCode " +
                "where parcelStatus <> co.enoobong.sendit.model.db.ParcelStatus.DELIVERED AND createdBy =:userId AND id =:parcelId"
    )
    fun changeUserUndeliveredParcelDestination(
        userId: Long, parcelId: Long, streetNumber: Int, streetName: String, city: String, state: String,
        country: String, zipCode: String?
    ): Int

    @Modifying
    @Query(
        "UPDATE Parcel set " +
                "to.streetNumber =:streetNumber, to.streetName =:streetName, to.city = :city, to.state = :state, to.country = :country, to.zipCode = :zipCode " +
                "where parcelStatus <> co.enoobong.sendit.model.db.ParcelStatus.DELIVERED AND id =:parcelId"
    )
    fun changeUndeliveredParcelDestination(
        parcelId: Long, streetNumber: Int, streetName: String, city: String, state: String,
        country: String, zipCode: String?
    ): Int

    @Modifying
    @Query(
        "UPDATE Parcel set " +
                "currentLocation.streetNumber =:streetNumber, currentLocation.streetName =:streetName, currentLocation.city = :city, currentLocation.state = :state " +
                ", currentLocation.country = :country, currentLocation.zipCode = :zipCode where id =:parcelId"
    )
    fun updateCurrentLocationById(
        parcelId: Long, streetNumber: Int, streetName: String, city: String, state: String,
        country: String, zipCode: String?
    ): Int

    @Modifying
    @Query("UPDATE Parcel SET parcelStatus =:newStatus WHERE id = :parcelId")
    fun updateParcelStatusById(parcelId: Long, newStatus: ParcelStatus): Int
}
