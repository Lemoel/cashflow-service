package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LancamentoRepository : JpaRepository<Lancamento, UUID> {
    /**
     * Inserir o lançamento só se ainda não existir uma linha com o mesmo (codigo_transacao, tipo_evento, parcela).
     * Se já existir: não fazer nada e não dar erro (idempotência).
     */
    @Modifying
    @Query(
        value =
            """
            INSERT INTO lancamento (
                id, nsu, tid, codigo_transacao, parcela, tipo_evento, meio_captura, valor_parcela,
                meio_pagamento, estabelecimento, pagamento_prazo, taxa_intermediacao,
                numero_serie_leitor, valor_total_transacao, data_inicial_transacao, hora_inicial_transacao,
                data_prevista_pagamento, valor_liquido_transacao, valor_original_transacao,
                maquina_id, congregacao_id, departamento_id,
                created_by_id, dti_created_date, last_modified_by_id, dti_last_modified_date
            ) VALUES (
                gen_random_uuid(),
                :nsu,
                :tid,
                :codigoTransacao,
                :parcela,
                :tipoEvento,
                :meioCaptura,
                :valorParcela,
                :meioPagamento,
                :estabelecimento,
                :pagamentoPrazo,
                :taxaIntermediacao,
                :numeroSerieLeitor,
                :valorTotalTransacao,
                :dataInicialTransacao,
                :horaInicialTransacao,
                :dataPrevistaPagamento,
                :valorLiquidoTransacao,
                :valorOriginalTransacao,
                :maquinaId,
                :congregacaoId,
                :departamentoId,
                :createdBy,
                CURRENT_TIMESTAMP,
                :lastModifiedBy,
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING
            """,
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
