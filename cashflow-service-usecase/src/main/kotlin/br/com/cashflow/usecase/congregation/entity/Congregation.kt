package br.com.cashflow.usecase.congregation.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "congregacao")
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

) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
