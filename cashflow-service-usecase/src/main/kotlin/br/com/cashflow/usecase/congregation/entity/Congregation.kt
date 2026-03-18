package br.com.cashflow.usecase.congregation.entity

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
@Table(name = "congregacao")
@EntityListeners(AuditingEntityListener::class)
class Congregation(
    @Id
    var id: UUID? = null,

    @Column(name = "tenant_id")
    var tenantId: UUID? = null,

    @Column(name = "setorial_id")
    var setorialId: UUID? = null,

    @Column(name = "nome")
    var nome: String = "",

    @Column(name = "cnpj")
    var cnpj: String? = null,

    @Column(name = "logradouro")
    var logradouro: String = "",

    @Column(name = "bairro")
    var bairro: String = "",

    @Column(name = "numero")
    var numero: String = "",

    @Column(name = "cidade")
    var cidade: String = "",

    @Column(name = "uf")
    var uf: String = "",

    @Column(name = "cep")
    var cep: String = "",

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "telefone")
    var telefone: String? = null,

    @Column(name = "ativo")
    var ativo: Boolean = true,

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
) {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
