package br.com.cashflow.usecase.lancamento.entity

import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "lancamento")
class Lancamento(
    @Id
    var id: UUID? = null,

    @Column(name = "nsu")
    val nsu: String? = null,

    @Column(name = "tid")
    val tid: String? = null,

    @Column(name = "codigo_transacao")
    val codigoTransacao: String? = null,

    @Column(name = "parcela")
    val parcela: String = "",

    @Column(name = "tipo_evento")
    @Enumerated(EnumType.STRING)
    val tipoEvento: TipoEventoEnum = TipoEventoEnum.DESCONHECIDO,

    @Column(name = "meio_captura")
    @Enumerated(EnumType.STRING)
    val meioCaptura: MeioCapturaEnum = MeioCapturaEnum.OUTRO,

    @Column(name = "valor_parcela")
    val valorParcela: BigDecimal = BigDecimal.ZERO,

    @Column(name = "meio_pagamento")
    @Enumerated(EnumType.STRING)
    val meioPagamento: MeioPagamentoEnum = MeioPagamentoEnum.OUTRO,

    @Column(name = "estabelecimento")
    val estabelecimento: String = "",

    @Column(name = "pagamento_prazo")
    val pagamentoPrazo: String = "",

    @Column(name = "taxa_intermediacao")
    val taxaIntermediacao: BigDecimal = BigDecimal.ZERO,

    @Column(name = "numero_serie_leitor")
    val numeroSerieLeitor: String? = null,

    @Column(name = "valor_total_transacao")
    val valorTotalTransacao: BigDecimal = BigDecimal.ZERO,

    @Column(name = "data_inicial_transacao")
    val dataInicialTransacao: LocalDate? = null,

    @Column(name = "hora_inicial_transacao")
    val horaInicialTransacao: String = "",

    @Column(name = "data_prevista_pagamento")
    val dataPrevistaPagamento: LocalDate? = null,

    @Column(name = "valor_liquido_transacao")
    val valorLiquidoTransacao: BigDecimal = BigDecimal.ZERO,

    @Column(name = "valor_original_transacao")
    val valorOriginalTransacao: BigDecimal = BigDecimal.ZERO,

    @Column(name = "maquina_id")
    val maquinaId: UUID? = null,

    @Column(name = "congregacao_id")
    val congregacaoId: UUID? = null,

    @Column(name = "departamento_id")
    val departamentoId: UUID? = null,

    @Column(name = "creation_user_id")
    val creationUserId: String = "",

    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @Column(name = "mod_user_id")
    val modUserId: String? = null,
)
