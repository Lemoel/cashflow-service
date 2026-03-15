package br.com.cashflow.usecase.lancamento.entity

import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table("lancamento")
class Lancamento(
    @Id
    var id: UUID? = null,

    @Column("nsu")
    val nsu: String? = null,

    @Column("tid")
    val tid: String? = null,

    @Column("codigo_transacao")
    val codigoTransacao: String? = null,

    @Column("parcela")
    val parcela: String = "",

    @Column("tipo_evento")
    val tipoEvento: TipoEventoEnum = TipoEventoEnum.DESCONHECIDO,

    @Column("meio_captura")
    val meioCaptura: MeioCapturaEnum = MeioCapturaEnum.OUTRO,

    @Column("valor_parcela")
    val valorParcela: BigDecimal = BigDecimal.ZERO,

    @Column("meio_pagamento")
    val meioPagamento: MeioPagamentoEnum = MeioPagamentoEnum.OUTRO,

    @Column("estabelecimento")
    val estabelecimento: String = "",

    @Column("pagamento_prazo")
    val pagamentoPrazo: String = "",

    @Column("taxa_intermediacao")
    val taxaIntermediacao: BigDecimal = BigDecimal.ZERO,

    @Column("numero_serie_leitor")
    val numeroSerieLeitor: String? = null,

    @Column("valor_total_transacao")
    val valorTotalTransacao: BigDecimal = BigDecimal.ZERO,

    @Column("data_inicial_transacao")
    val dataInicialTransacao: LocalDate? = null,

    @Column("hora_inicial_transacao")
    val horaInicialTransacao: String = "",

    @Column("data_prevista_pagamento")
    val dataPrevistaPagamento: LocalDate? = null,

    @Column("valor_liquido_transacao")
    val valorLiquidoTransacao: BigDecimal = BigDecimal.ZERO,

    @Column("valor_original_transacao")
    val valorOriginalTransacao: BigDecimal = BigDecimal.ZERO,

    @Column("maquina_id")
    val maquinaId: UUID? = null,

    @Column("congregacao_id")
    val congregacaoId: UUID? = null,

    @Column("departamento_id")
    val departamentoId: UUID? = null,

    @Column("creation_user_id")
    val creationUserId: String = "",

    @Column("created_at")
    var createdAt: Instant? = null,

    @Column("updated_at")
    var updatedAt: Instant? = null,

    @Column("mod_user_id")
    val modUserId: String? = null,
)
