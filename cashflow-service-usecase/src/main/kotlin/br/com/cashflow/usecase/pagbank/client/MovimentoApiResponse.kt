package br.com.cashflow.usecase.pagbank.client

import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MovimentoApiResponse(
    val detalhes: List<LancamentoDetalhe> = emptyList(),
    val pagination: Pagination? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LancamentoDetalhe(
    val nsu: String? = null,
    val tid: String? = null,
    val codigoTransacao: String? = null,
    val parcela: String = "",
    val tipoEvento: TipoEventoEnum = TipoEventoEnum.DESCONHECIDO,
    val meioCaptura: MeioCapturaEnum = MeioCapturaEnum.OUTRO,
    val valorParcela: BigDecimal = BigDecimal.ZERO,
    val meioPagamento: MeioPagamentoEnum = MeioPagamentoEnum.OUTRO,
    val estabelecimento: String = "",
    val pagamentoPrazo: String = "",
    val taxaIntermediacao: BigDecimal = BigDecimal.ZERO,
    val numeroSerieLeitor: String? = null,
    val valorTotalTransacao: BigDecimal = BigDecimal.ZERO,
    val dataInicialTransacao: LocalDate? = null,
    val horaInicialTransacao: String = "",
    val dataPrevistaPagamento: LocalDate? = null,
    val valorLiquidoTransacao: BigDecimal = BigDecimal.ZERO,
    val valorOriginalTransacao: BigDecimal = BigDecimal.ZERO,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Pagination(
    val totalPages: Int = 1,
    val page: Int = 1,
    val totalElements: Int = 0,
)
