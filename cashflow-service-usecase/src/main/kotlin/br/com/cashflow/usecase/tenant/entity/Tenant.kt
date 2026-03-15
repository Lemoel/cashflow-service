package br.com.cashflow.usecase.tenant.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table(value = "tenants", schema = "core")
class Tenant(
    @Id
    var id: UUID? = null,
    @Column("cnpj")
    var cnpj: String = "",
    @Column("nome_fantasia")
    var tradeName: String = "",
    @Column("razao_social")
    var companyName: String? = null,
    @Column("logradouro")
    var street: String = "",
    @Column("numero")
    var number: String = "",
    @Column("complemento")
    var complement: String? = null,
    @Column("bairro")
    var neighborhood: String? = null,
    @Column("cidade")
    var city: String = "",
    @Column("uf")
    var state: String = "",
    @Column("cep")
    var zipCode: String = "",
    @Column("telefone")
    var phone: String? = null,
    @Column("email")
    var email: String? = null,
    @Column("ativo")
    var active: Boolean = true,
    @Column("creation_user_id")
    var creationUserId: String = "",
    @Column("mod_user_id")
    var modUserId: String? = null,
    @Column("created_at")
    var createdAt: Instant? = null,
    @Column("updated_at")
    var updatedAt: Instant? = null,
    @Column("schema_name")
    var schemaName: String = "",
)
