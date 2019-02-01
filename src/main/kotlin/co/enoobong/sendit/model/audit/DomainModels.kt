package co.enoobong.sendit.model.audit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(
    value = ["createdAt", "updatedAt"],
    allowGetters = true
)
abstract class DateAudit : Serializable {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant
}

@MappedSuperclass
@JsonIgnoreProperties(value = ["createdBy", "updatedBy"], allowGetters = true)
abstract class UserDateAudit : DateAudit() {
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    var createdBy: Long = 0L

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    var updatedBy: Long = 0L
}