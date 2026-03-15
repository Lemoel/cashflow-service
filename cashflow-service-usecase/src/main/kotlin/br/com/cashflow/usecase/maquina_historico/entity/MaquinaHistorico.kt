package br.com.cashflow.usecase.maquina_historico.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("maquina_historico")
class MaquinaHistorico(
    @Id
    var id: UUID? = null,

    @Column("maquina_id")
    var maquinaId: UUID? = null,

    @Column("congregacao_id")
    var congregacaoId: UUID? = null,

    @Column("departamento_id")
    var departamentoId: UUID? = null,

    @Column("data_inicio")
    var dataInicio: Instant? = null,

    @Column("data_fim")
    var dataFim: Instant? = null,
)
