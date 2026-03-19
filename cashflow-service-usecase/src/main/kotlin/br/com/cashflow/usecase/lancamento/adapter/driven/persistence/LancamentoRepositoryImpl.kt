package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Types

@Repository
class LancamentoRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : LancamentoRepositoryCustom {
    override fun batchInsertIgnorandoDuplicatas(lancamentos: List<Lancamento>) {
        if (lancamentos.isEmpty()) return
        jdbcTemplate.batchUpdate(
            INSERT_SQL,
            object : BatchPreparedStatementSetter {
                override fun setValues(
                    ps: PreparedStatement,
                    i: Int,
                ) {
                    val l = lancamentos[i]
                    var idx = 1
                    ps.setObject(idx++, l.nsu, Types.VARCHAR)
                    ps.setObject(idx++, l.tid, Types.VARCHAR)
                    ps.setObject(idx++, l.codigoTransacao, Types.VARCHAR)
                    ps.setString(idx++, l.parcela)
                    ps.setString(idx++, l.tipoEvento.code)
                    ps.setString(idx++, l.meioCaptura.code)
                    ps.setBigDecimal(idx++, l.valorParcela)
                    ps.setString(idx++, l.meioPagamento.code)
                    ps.setString(idx++, l.estabelecimento)
                    ps.setString(idx++, l.pagamentoPrazo)
                    ps.setBigDecimal(idx++, l.taxaIntermediacao)
                    ps.setObject(idx++, l.numeroSerieLeitor, Types.VARCHAR)
                    ps.setBigDecimal(idx++, l.valorTotalTransacao)
                    ps.setObject(idx++, l.dataInicialTransacao, Types.DATE)
                    ps.setString(idx++, l.horaInicialTransacao)
                    ps.setObject(idx++, l.dataPrevistaPagamento, Types.DATE)
                    ps.setBigDecimal(idx++, l.valorLiquidoTransacao)
                    ps.setBigDecimal(idx++, l.valorOriginalTransacao)
                    ps.setObject(idx++, l.maquinaId, Types.OTHER)
                    ps.setObject(idx++, l.congregacaoId, Types.OTHER)
                    ps.setObject(idx++, l.departamentoId, Types.OTHER)
                    ps.setString(idx++, l.createdBy ?: "system")
                    ps.setString(idx, l.lastModifiedBy ?: l.createdBy ?: "system")
                }

                override fun getBatchSize(): Int = lancamentos.size
            },
        )
    }

    companion object {
        private val INSERT_SQL =
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
                ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?,
                ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP
            )
            ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING
            """.trimIndent()
    }
}
