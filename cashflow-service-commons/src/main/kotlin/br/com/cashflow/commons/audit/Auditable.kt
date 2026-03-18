package br.com.cashflow.commons.audit

import jakarta.persistence.Column
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

abstract class Auditable {
    @CreatedBy
    @Column(name = "created_by")
    var createdBy: String? = null

    @CreatedDate
    @Column(name = "created_date")
    var createdDate: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null

    @LastModifiedDate
    @Column(name = "last_modified_date")
    var lastModifiedDate: LocalDateTime? = null
}
