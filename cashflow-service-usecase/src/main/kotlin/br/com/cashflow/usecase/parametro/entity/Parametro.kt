package br.com.cashflow.usecase.parametro.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("parametro")
class Parametro(
    @Id
    var id: UUID? = null,

    @Column("chave")
    var chave: String = "",

    @Column("valor_texto")
    var valorTexto: String? = null,

    @Column("valor_inteiro")
    var valorInteiro: Long? = null,

    @Column("valor_decimal")
    var valorDecimal: Double? = null,

    @Column("tipo")
    var tipo: String = "",

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
