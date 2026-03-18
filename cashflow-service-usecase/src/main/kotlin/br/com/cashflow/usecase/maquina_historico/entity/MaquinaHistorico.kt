package br.com.cashflow.usecase.maquina_historico.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "maquina_historico")
class MaquinaHistorico(
    @Id
    var id: UUID? = null,

    @Column(name = "maquina_id")
    var maquinaId: UUID? = null,

    @Column(name = "congregacao_id")
    var congregacaoId: UUID? = null,

    @Column(name = "departamento_id")
    var departamentoId: UUID? = null,

    @Column(name = "data_inicio")
    var dataInicio: Instant? = null,

    @Column(name = "data_fim")
    var dataFim: Instant? = null,
)
