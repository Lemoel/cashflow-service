package br.com.cashflow.usecase.department.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "departamento")
class Department(
    @Id
    var id: UUID? = null,

    @Column(name = "tenant_id")
    var tenantId: UUID? = null,

    @Column(name = "nome")
    var nome: String = "",

    @Column(name = "ativo")
    var ativo: Boolean = true,
) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
