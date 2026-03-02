package br.com.cashflow.commons.audit

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

abstract class Auditable {
    @CreatedBy
    @Column("created_by")
    var createdBy: String? = null

    @CreatedDate
    @Column("created_date")
    var createdDate: LocalDateTime? = null

    @LastModifiedBy
    @Column("last_modified_by")
    var lastModifiedBy: String? = null

    @LastModifiedDate
    @Column("last_modified_date")
    var lastModifiedDate: LocalDateTime? = null
}
