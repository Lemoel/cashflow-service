package br.com.cashflow.usecase.tenant.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tenants", schema = "core")
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

    @Column(name = "schema_name")
    var schemaName: String = "",
) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
