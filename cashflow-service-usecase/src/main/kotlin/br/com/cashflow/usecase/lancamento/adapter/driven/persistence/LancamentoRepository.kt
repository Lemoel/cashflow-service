package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LancamentoRepository :
    JpaRepository<Lancamento, UUID>,
    LancamentoRepositoryCustom {
    /**
     * Inserir o lançamento só se ainda não existir uma linha com o mesmo (codigo_transacao, tipo_evento, parcela).
     * Se já existir: não fazer nada e não dar erro (idempotência).
     */
    @Modifying
    @Query(
        value = LancamentoInsertStatements.INSERT_ONE_IGNORING_DUPLICATES_NATIVE,
        nativeQuery = true,
    )
    fun insertIgnorandoDuplicata(
        @Param("nsu") nsu: String?,
        @Param("tid") tid: String?,
        @Param("codigoTransacao") codigoTransacao: String?,
        @Param("parcela") parcela: String,
        @Param("tipoEvento") tipoEvento: String,
        @Param("meioCaptura") meioCaptura: String,
        @Param("valorParcela") valorParcela: java.math.BigDecimal,
        @Param("meioPagamento") meioPagamento: String,
        @Param("estabelecimento") estabelecimento: String,
        @Param("pagamentoPrazo") pagamentoPrazo: String,
        @Param("taxaIntermediacao") taxaIntermediacao: java.math.BigDecimal,
        @Param("numeroSerieLeitor") numeroSerieLeitor: String?,
        @Param("valorTotalTransacao") valorTotalTransacao: java.math.BigDecimal,
        @Param("dataInicialTransacao") dataInicialTransacao: java.time.LocalDate?,
        @Param("horaInicialTransacao") horaInicialTransacao: String,
        @Param("dataPrevistaPagamento") dataPrevistaPagamento: java.time.LocalDate?,
        @Param("valorLiquidoTransacao") valorLiquidoTransacao: java.math.BigDecimal,
        @Param("valorOriginalTransacao") valorOriginalTransacao: java.math.BigDecimal,
        @Param("maquinaId") maquinaId: UUID?,
        @Param("congregacaoId") congregacaoId: UUID?,
        @Param("departamentoId") departamentoId: UUID?,
        @Param("createdBy") createdBy: String,
        @Param("lastModifiedBy") lastModifiedBy: String,
    )
}
