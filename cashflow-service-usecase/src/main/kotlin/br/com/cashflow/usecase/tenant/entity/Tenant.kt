package br.com.cashflow.usecase.tenant.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenants", schema = "core")
@EntityListeners(AuditingEntityListener::class)
class Tenant(
    @Id
    var id: UUID? = null,

    @Column(name = "cnpj")
    var cnpj: String = "",

    @Column(name = "nome_fantasia")
    var tradeName: String = "",

    @Column(name = "razao_social")
    var companyName: String? = null,

    @Column(name = "logradouro")
    var street: String = "",

    @Column(name = "numero")
    var number: String = "",

    @Column(name = "complemento")
    var complement: String? = null,

    @Column(name = "bairro")
    var neighborhood: String? = null,

    @Column(name = "cidade")
    var city: String = "",

    @Column(name = "uf", length = 2)
    var state: String = "",

    @Column(name = "cep")
    var zipCode: String = "",

    @Column(name = "telefone")
    var phone: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "ativo")
    var active: Boolean = true,

    @CreatedBy
    @Column(name = "creation_user_id")
    var creationUserId: String = "",

    @LastModifiedBy
    @Column(name = "mod_user_id")
    var modUserId: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @Column(name = "schema_name")
    var schemaName: String = "",
) {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
