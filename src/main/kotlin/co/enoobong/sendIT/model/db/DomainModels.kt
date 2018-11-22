package co.enoobong.sendIT.model.db

import co.enoobong.sendIT.model.audit.DateAudit
import org.hibernate.annotations.NaturalId
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
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
    @Column(length = 60, nullable = false)
    val name: RoleName,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)

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
    val roles: Set<Role> = setOf()
) : DateAudit() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (otherNames != other.otherNames) return false
        if (username != other.username) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (id != other.id) return false
        if (roles != other.roles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (otherNames?.hashCode() ?: 0)
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + roles.hashCode()
        return result
    }
}
