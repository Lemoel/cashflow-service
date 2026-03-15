package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.util.UUID

@Repository
class MaquinaHistoricoRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : MaquinaHistoricoRepositoryCustom {
    override fun findByMaquinaIdOrderByDataInicioDesc(maquinaId: UUID): List<MaquinaHistoricoItemRow> {
        val sql =
            """
            SELECT
                h.id,
                h.maquina_id,
                h.congregacao_id,
                c.nome AS congregacao_nome,
                h.departamento_id,
                d.nome AS departamento_nome,
                h.data_inicio,
                h.data_fim
            FROM maquina_historico h
            LEFT JOIN congregacao c ON h.congregacao_id = c.id
            LEFT JOIN departamento d ON h.departamento_id = d.id
            WHERE h.maquina_id = ?
            ORDER BY h.data_inicio DESC
            """.trimIndent()
        return jdbcTemplate.query(
            sql,
            PreparedStatementSetter { ps -> ps.setObject(1, maquinaId, Types.OTHER) },
            MAQUINA_HISTORICO_ROW_MAPPER,
        )
    }

    companion object {
        private val MAQUINA_HISTORICO_ROW_MAPPER =
            RowMapper<MaquinaHistoricoItemRow> { rs: ResultSet, _: Int ->
                MaquinaHistoricoItemRow(
                    id = uuid(rs, "id")!!,
                    maquinaId = uuid(rs, "maquina_id")!!,
                    congregacaoId = uuid(rs, "congregacao_id"),
                    congregacaoNome = rs.getString("congregacao_nome"),
                    departamentoId = uuid(rs, "departamento_id"),
                    departamentoNome = rs.getString("departamento_nome"),
                    dataInicio = instant(rs, "data_inicio")!!,
                    dataFim = instant(rs, "data_fim"),
                )
            }

        private fun uuid(
            rs: ResultSet,
            column: String,
        ): UUID? {
            val s = rs.getString(column) ?: return null
            return try {
                UUID.fromString(s)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        private fun instant(
            rs: ResultSet,
            column: String,
        ): Instant? {
            val ts = rs.getTimestamp(column) ?: return null
            return ts.toInstant()
        }
    }
}
