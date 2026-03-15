package br.com.cashflow.usecase.maquina.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("maquina")
class Maquina(
    @Id
    var id: UUID? = null,

    @Column("numero_serie_leitor")
    var numeroSerieLeitor: String? = null,

    @Column("congregacao_id")
    var congregacaoId: UUID? = null,

    @Column("banco_id")
    var bancoId: UUID? = null,

    @Column("departamento_id")
    var departamentoId: UUID? = null,

    @Column("ativo")
    var ativo: Boolean = true,

    @Version
    @Column("version")
    var version: Long? = null,

    @Column("created_at")
    var createdAt: Instant? = null,

    @Column("updated_at")
    var updatedAt: Instant? = null,

    @Column("creation_user_id")
    var creationUserId: String = "",

    @Column("mod_user_id")
    var modUserId: String? = null,
)
