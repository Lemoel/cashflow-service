package br.com.cashflow.commons.audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class Auditable<T> {

    @CreatedBy
    @Column(name = "created_by_id", updatable = false)
    open var createdBy: T? = null

    @CreatedDate
    @Column(name = "dti_created_date", updatable = false)
    open var createdDate: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "last_modified_by_id")
    open var lastModifiedBy: T? = null

    @LastModifiedDate
    @Column(name = "dti_last_modified_date")
    open var lastModifiedDate: LocalDateTime? = null
}
