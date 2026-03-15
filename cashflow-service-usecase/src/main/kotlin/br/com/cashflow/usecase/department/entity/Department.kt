package br.com.cashflow.usecase.department.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("departamento")
class Department(

    @Id
    var id: UUID? = null,

    @Column("tenant_id")
    var tenantId: UUID? = null,

    @Column("nome")
    var nome: String = "",

    @Column("ativo")
    var ativo: Boolean = true,

    @Column("creation_user_id")
    var creationUserId: String = "",

    @Column("mod_user_id")
    var modUserId: String? = null,

    @Column("created_at")
    var createdAt: Instant? = null,

    @Column("updated_at")
    var updatedAt: Instant? = null,

)
