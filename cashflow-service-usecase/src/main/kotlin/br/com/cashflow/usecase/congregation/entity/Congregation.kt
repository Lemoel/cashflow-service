package br.com.cashflow.usecase.congregation.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table(value = "congregacao", schema = "eventos")
class Congregation(

    @Id
    var id: UUID? = null,

    @Column("tenant_id")
    var tenantId: UUID? = null,

    @Column("setorial_id")
    var setorialId: UUID? = null,

    @Column("nome")
    var nome: String = "",

    @Column("cnpj")
    var cnpj: String? = null,

    @Column("logradouro")
    var logradouro: String = "",

    @Column("bairro")
    var bairro: String = "",

    @Column("numero")
    var numero: String = "",

    @Column("cidade")
    var cidade: String = "",

    @Column("uf")
    var uf: String = "",

    @Column("cep")
    var cep: String = "",

    @Column("email")
    var email: String? = null,

    @Column("telefone")
    var telefone: String? = null,

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
