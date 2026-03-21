package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.Session
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Date
import java.util.UUID

@Repository
class LancamentoRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager,
) : LancamentoRepositoryCustom {
    override fun batchInsertIgnorandoDuplicatas(lancamentos: List<Lancamento>) {
        if (lancamentos.isEmpty()) return
        lancamentos.chunked(LANCAMENTO_BULK_CHUNK_SIZE).forEach { chunk ->
            insertChunkUnnest(chunk)
        }
    }

    private fun insertChunkUnnest(chunk: List<Lancamento>) {
        val n = chunk.size
        val nsus = arrayOfNulls<String>(n)
        val tids = arrayOfNulls<String>(n)
        val codigoTransacoes = arrayOfNulls<String>(n)
        val parcelas = arrayOfNulls<String>(n)
        val tipoEventos = arrayOfNulls<String>(n)
        val meioCapturas = arrayOfNulls<String>(n)
        val valorParcelas = arrayOfNulls<BigDecimal>(n)
        val meioPagamentos = arrayOfNulls<String>(n)
        val estabelecimentos = arrayOfNulls<String>(n)
        val pagamentoPrazos = arrayOfNulls<String>(n)
        val taxaIntermediacoes = arrayOfNulls<BigDecimal>(n)
        val numeroSeriesLeitor = arrayOfNulls<String>(n)
        val valorTotalTransacoes = arrayOfNulls<BigDecimal>(n)
        val dataInicialTransacoes = arrayOfNulls<Date>(n)
        val horaInicialTransacoes = arrayOfNulls<String>(n)
        val dataPrevistaPagamentos = arrayOfNulls<Date>(n)
        val valorLiquidoTransacoes = arrayOfNulls<BigDecimal>(n)
        val valorOriginalTransacoes = arrayOfNulls<BigDecimal>(n)
        val maquinaIds = arrayOfNulls<UUID>(n)
        val congregacaoIds = arrayOfNulls<UUID>(n)
        val departamentoIds = arrayOfNulls<UUID>(n)
        val createdBys = arrayOfNulls<String>(n)
        val lastModifiedBys = arrayOfNulls<String>(n)

        chunk.forEachIndexed { i, l ->
            nsus[i] = l.nsu
            tids[i] = l.tid
            codigoTransacoes[i] = l.codigoTransacao
            parcelas[i] = l.parcela
            tipoEventos[i] = l.tipoEvento.code
            meioCapturas[i] = l.meioCaptura.code
            valorParcelas[i] = l.valorParcela
            meioPagamentos[i] = l.meioPagamento.code
            estabelecimentos[i] = l.estabelecimento
            pagamentoPrazos[i] = l.pagamentoPrazo
            taxaIntermediacoes[i] = l.taxaIntermediacao
            numeroSeriesLeitor[i] = l.numeroSerieLeitor
            valorTotalTransacoes[i] = l.valorTotalTransacao
            dataInicialTransacoes[i] = l.dataInicialTransacao?.let { Date.valueOf(it) }
            horaInicialTransacoes[i] = l.horaInicialTransacao
            dataPrevistaPagamentos[i] = l.dataPrevistaPagamento?.let { Date.valueOf(it) }
            valorLiquidoTransacoes[i] = l.valorLiquidoTransacao
            valorOriginalTransacoes[i] = l.valorOriginalTransacao
            maquinaIds[i] = l.maquinaId
            congregacaoIds[i] = l.congregacaoId
            departamentoIds[i] = l.departamentoId
            createdBys[i] = l.createdBy ?: "system"
            lastModifiedBys[i] = l.lastModifiedBy ?: l.createdBy ?: "system"
        }

        entityManager.unwrap(Session::class.java).doWork { connection ->
            connection.prepareStatement(BULK_INSERT_UNNEST_SQL).use { ps ->
                ps.setArray(1, connection.createArrayOf("varchar", nsus))
                ps.setArray(2, connection.createArrayOf("varchar", tids))
                ps.setArray(3, connection.createArrayOf("varchar", codigoTransacoes))
                ps.setArray(4, connection.createArrayOf("varchar", parcelas))
                ps.setArray(5, connection.createArrayOf("varchar", tipoEventos))
                ps.setArray(6, connection.createArrayOf("varchar", meioCapturas))
                ps.setArray(7, connection.createArrayOf("numeric", valorParcelas))
                ps.setArray(8, connection.createArrayOf("varchar", meioPagamentos))
                ps.setArray(9, connection.createArrayOf("varchar", estabelecimentos))
                ps.setArray(10, connection.createArrayOf("varchar", pagamentoPrazos))
                ps.setArray(11, connection.createArrayOf("numeric", taxaIntermediacoes))
                ps.setArray(12, connection.createArrayOf("varchar", numeroSeriesLeitor))
                ps.setArray(13, connection.createArrayOf("numeric", valorTotalTransacoes))
                ps.setArray(14, connection.createArrayOf("date", dataInicialTransacoes))
                ps.setArray(15, connection.createArrayOf("varchar", horaInicialTransacoes))
                ps.setArray(16, connection.createArrayOf("date", dataPrevistaPagamentos))
                ps.setArray(17, connection.createArrayOf("numeric", valorLiquidoTransacoes))
                ps.setArray(18, connection.createArrayOf("numeric", valorOriginalTransacoes))
                ps.setArray(19, connection.createArrayOf("uuid", maquinaIds))
                ps.setArray(20, connection.createArrayOf("uuid", congregacaoIds))
                ps.setArray(21, connection.createArrayOf("uuid", departamentoIds))
                ps.setArray(22, connection.createArrayOf("varchar", createdBys))
                ps.setArray(23, connection.createArrayOf("varchar", lastModifiedBys))
                ps.executeUpdate()
            }
        }
    }

    companion object {
        private const val LANCAMENTO_BULK_CHUNK_SIZE = 500

        private val BULK_INSERT_UNNEST_SQL =
            """
            INSERT INTO lancamento (${LancamentoInsertStatements.LANCAMENTO_INSERT_COLUMNS})
            SELECT
                gen_random_uuid(),
                u.nsu,
                u.tid,
                u.codigo_transacao,
                u.parcela,
                u.tipo_evento,
                u.meio_captura,
                u.valor_parcela,
                u.meio_pagamento,
                u.estabelecimento,
                u.pagamento_prazo,
                u.taxa_intermediacao,
                u.numero_serie_leitor,
                u.valor_total_transacao,
                u.data_inicial_transacao,
                u.hora_inicial_transacao,
                u.data_prevista_pagamento,
                u.valor_liquido_transacao,
                u.valor_original_transacao,
                u.maquina_id,
                u.congregacao_id,
                u.departamento_id,
                u.created_by_id,
                CURRENT_TIMESTAMP,
                u.last_modified_by_id,
                CURRENT_TIMESTAMP
            FROM ROWS FROM (
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::numeric[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::varchar[]),
                unnest(?::numeric[]),
                unnest(?::varchar[]),
                unnest(?::numeric[]),
                unnest(?::date[]),
                unnest(?::varchar[]),
                unnest(?::date[]),
                unnest(?::numeric[]),
                unnest(?::numeric[]),
                unnest(?::uuid[]),
                unnest(?::uuid[]),
                unnest(?::uuid[]),
                unnest(?::varchar[]),
                unnest(?::varchar[])
            ) AS u (
                nsu,
                tid,
                codigo_transacao,
                parcela,
                tipo_evento,
                meio_captura,
                valor_parcela,
                meio_pagamento,
                estabelecimento,
                pagamento_prazo,
                taxa_intermediacao,
                numero_serie_leitor,
                valor_total_transacao,
                data_inicial_transacao,
                hora_inicial_transacao,
                data_prevista_pagamento,
                valor_liquido_transacao,
                valor_original_transacao,
                maquina_id,
                congregacao_id,
                departamento_id,
                created_by_id,
                last_modified_by_id
            )
            """.trimIndent() + " " + LancamentoInsertStatements.LANCAMENTO_ON_CONFLICT_IGNORE_DUPLICATES
    }
}
