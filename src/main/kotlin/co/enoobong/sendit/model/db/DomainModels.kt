package co.enoobong.sendit.model.db

import co.enoobong.sendit.model.audit.DateAudit
import co.enoobong.sendit.model.audit.UserDateAudit
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.NaturalId
import java.time.Instant
import javax.persistence.AttributeOverride
import javax.persistence.AttributeOverrides
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size


enum class RoleName {
    ROLE_USER,
    ROLE_ADMIN
}

@Entity
@Table(name = "roles", uniqueConstraints = [UniqueConstraint(columnNames = ["name"])])
class Role(
    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60, nullable = false, unique = true)
    val name: RoleName,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Role

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

@Entity
@Table(
    name = "users", uniqueConstraints = [
        UniqueConstraint(
            columnNames = [
                "user_name"
            ]
        ),
        UniqueConstraint(
            columnNames = [
                "email"
            ]
        )
    ]
)
class User(
    @field:NotBlank
    @field:Size(max = 40)
    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @field:NotBlank
    @field:Size(max = 40)
    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "other_names")
    val otherNames: String?,

    @field:NotBlank
    @field:Size(max = 15)
    @Column(name = "user_name", nullable = false, unique = true)
    val username: String,

    @NaturalId
    @field:NotBlank
    @field:Size(max = 40)
    @Email
    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @field:NotBlank
    @field:Size(max = 100)
    @Column(name = "password", nullable = false)
    val password: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf()
) : DateAudit() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (username != other.username) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + username.hashCode()
        return result
    }
}

@Entity
@Table(name = "parcel", indexes = [Index(columnList = "created_by")])
class Parcel(
    @field:Positive
    @Column(name = "weight", nullable = false)
    val weight: Float,

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_metric", nullable = false)
    val weightMetric: WeightMetric,

    @Enumerated(EnumType.STRING)
    @Column(name = "parcel_status", nullable = false)
    val parcelStatus: ParcelStatus,

    @Embedded
    @field:Valid
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "streetNumber", column = Column(name = "from_street_number")),
            AttributeOverride(name = "streetName", column = Column(name = "from_street_name")),
            AttributeOverride(name = "city", column = Column(name = "from_city")),
            AttributeOverride(name = "state", column = Column(name = "from_state")),
            AttributeOverride(name = "country", column = Column(name = "from_country")),
            AttributeOverride(name = "zipCode", column = Column(name = "from_zip_code"))
        ]
    )
    val from: Address,

    @Embedded
    @field:Valid
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "streetNumber", column = Column(name = "to_street_number")),
            AttributeOverride(name = "streetName", column = Column(name = "to_street_name")),
            AttributeOverride(name = "city", column = Column(name = "to_city")),
            AttributeOverride(name = "state", column = Column(name = "to_state")),
            AttributeOverride(name = "country", column = Column(name = "to_country")),
            AttributeOverride(name = "zipCode", column = Column(name = "to_zip_code"))
        ]
    )
    val to: Address,


    @Embedded
    @field:Valid
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "streetNumber", column = Column(name = "current_location_street_number")),
            AttributeOverride(name = "streetName", column = Column(name = "current_location_street_name")),
            AttributeOverride(name = "city", column = Column(name = "current_location_city")),
            AttributeOverride(name = "state", column = Column(name = "current_location_state")),
            AttributeOverride(name = "country", column = Column(name = "current_location_country")),
            AttributeOverride(name = "zipCode", column = Column(name = "current_location_zip_code"))
        ]
    )
    val currentLocation: Address,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : UserDateAudit() {

    @Column(name = "delivered_on")
    val deliveredOn: Instant? = null

    @Column(name = "sent_on")
    val sentOn: Instant? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Parcel

        if (createdBy != other.createdBy) return false
        if (weight != other.weight) return false
        if (weightMetric != other.weightMetric) return false
        if (parcelStatus != other.parcelStatus) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (currentLocation != other.currentLocation) return false
        if (deliveredOn != other.deliveredOn) return false
        if (sentOn != other.sentOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = weight.hashCode()
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + weightMetric.hashCode()
        result = 31 * result + parcelStatus.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + currentLocation.hashCode()
        result = 31 * result + (deliveredOn?.hashCode() ?: 0)
        result = 31 * result + (sentOn?.hashCode() ?: 0)
        return result
    }
}

@Embeddable
data class Address(
    @field:Positive
    @field:JsonProperty("street_number")
    @Column(name = "street_number", nullable = false)
    val streetNumber: Int,

    @field:NotBlank
    @field:JsonProperty("street_name")
    @Column(name = "street_name", nullable = false)
    val streetName: String,

    @field:NotBlank
    @field:JsonProperty("city")
    @Column(name = "city", nullable = false)
    val city: String,

    @field:NotBlank
    @Column(name = "state", nullable = false)
    val state: String,

    @field:NotBlank
    @Column(name = "country", nullable = false)
    val country: String,

    @Column(name = "zip_code")
    @field:JsonProperty("zip_code")
    val zipCode: String? = null
) {
    fun displayableAddress(): String {
        return "$streetNumber $streetName, $city, $state, $country"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (streetNumber != other.streetNumber) return false
        if (streetName != other.streetName) return false
        if (city != other.city) return false
        if (state != other.state) return false
        if (country != other.country) return false
        if (zipCode != other.zipCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = streetNumber
        result = 31 * result + streetName.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + (zipCode?.hashCode() ?: 0)
        return result
    }


}

enum class WeightMetric {
    KG,
    POUND
}

enum class ParcelStatus {
    PLACED,
    TRANSITING,
    DELIVERED,
    CANCELLED
}
