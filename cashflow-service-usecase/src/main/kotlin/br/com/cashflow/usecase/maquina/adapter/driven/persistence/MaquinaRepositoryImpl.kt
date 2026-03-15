package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@Repository
class MaquinaRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : MaquinaRepositoryCustom {
    override fun findByIdWithDetalhes(id: UUID): MaquinaComCongregacao? {
        val sql =
            """
            SELECT
                m.id,
                m.numero_serie_leitor AS maquina_id,
                m.congregacao_id,
                COALESCE(c.nome, 'Não informada') AS congregacao_nome,
                m.banco_id,
                COALESCE(b.nome, 'Não informado') AS banco_nome,
                m.departamento_id,
                d.nome AS departamento_nome,
                m.ativo,
                m.version,
                m.created_at,
                m.updated_at
            FROM maquina m
            LEFT JOIN congregacao c ON m.congregacao_id = c.id
            LEFT JOIN banco b ON m.banco_id = b.id
            LEFT JOIN departamento d ON m.departamento_id = d.id
            WHERE m.id = ?
            """.trimIndent()
        val list = jdbcTemplate.query(sql, MAQUINA_COM_CONGREGACAO_ROW_MAPPER, id)
        return list.firstOrNull()
    }

    override fun findWithFiltersComDetalhes(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        if (!maquinaId.isNullOrBlank()) {
            conditions.add("(m.numero_serie_leitor::text ILIKE ?)")
            params.add("%$maquinaId%")
        }

        if (!congregacao.isNullOrBlank()) {
            conditions.add("(c.nome ILIKE ?)")
            params.add("%$congregacao%")
        }

        if (!banco.isNullOrBlank()) {
            conditions.add("(b.nome ILIKE ?)")
            params.add("%$banco%")
        }

        if (departamentoId != null) {
            conditions.add("m.departamento_id = ?")
            params.add(departamentoId)
        }

        return findMaquinaComCongregacaoPaginated(conditions, params, page, size)
    }

    override fun findParaSelecaoHistorico(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerieLeitor: String?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()
        if (tenantId != null) {
            conditions.add("c.tenant_id = ?")
            params.add(tenantId)
        }
        if (congregacaoId != null) {
            conditions.add("m.congregacao_id = ?")
            params.add(congregacaoId)
        }
        if (!numeroSerieLeitor.isNullOrBlank()) {
            conditions.add("(m.numero_serie_leitor::text ILIKE ?)")
            params.add("%$numeroSerieLeitor%")
        }
        return findMaquinaComCongregacaoPaginated(conditions, params, page, size)
    }

    private fun findMaquinaComCongregacaoPaginated(
        conditions: List<String>,
        params: MutableList<Any>,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val whereClause =
            if (conditions.isEmpty()) {
                ""
            } else {
                " WHERE " +
                    conditions.joinToString(" AND ")
            }
        val countSql = "SELECT COUNT(*) $MAQUINA_FROM_CLAUSE$whereClause"
        val total =
            jdbcTemplate.queryForObject(countSql, Long::class.java, *params.toTypedArray()) ?: 0L
        params.add(size)
        params.add(page * size)
        val selectSql =
            """
            SELECT
            $MAQUINA_SELECT_COLUMNS
            $MAQUINA_FROM_CLAUSE$whereClause
            ORDER BY m.numero_serie_leitor
            LIMIT ? OFFSET ?
            """.trimIndent()
        val items =
            jdbcTemplate.query(
                selectSql,
                MAQUINA_COM_CONGREGACAO_ROW_MAPPER,
                *params.toTypedArray(),
            )
        return MaquinaQueryResult(items = items, total = total)
    }

    companion object {
        private val MAQUINA_FROM_CLAUSE =
            """
            FROM maquina m
            LEFT JOIN congregacao c ON m.congregacao_id = c.id
            LEFT JOIN banco b ON m.banco_id = b.id
            LEFT JOIN departamento d ON m.departamento_id = d.id
            """.trimIndent()

        private val MAQUINA_SELECT_COLUMNS =
            """
            m.id,
            m.numero_serie_leitor AS maquina_id,
            m.congregacao_id,
            COALESCE(c.nome, 'Não informada') AS congregacao_nome,
            m.banco_id,
            COALESCE(b.nome, 'Não informado') AS banco_nome,
            m.departamento_id,
            d.nome AS departamento_nome,
            m.ativo,
            m.version,
            m.created_at,
            m.updated_at
            """.trimIndent()

        private val MAQUINA_COM_CONGREGACAO_ROW_MAPPER =
            RowMapper<MaquinaComCongregacao> { rs: ResultSet, _: Int ->
                MaquinaComCongregacao(
                    id = uuid(rs, "id")!!,
                    maquinaId = rs.getString("maquina_id") ?: "",
                    congregacaoId = uuid(rs, "congregacao_id"),
                    congregacaoNome = rs.getString("congregacao_nome") ?: "",
                    bancoId = uuid(rs, "banco_id"),
                    bancoNome = rs.getString("banco_nome") ?: "",
                    departamentoId = uuid(rs, "departamento_id"),
                    departamentoNome = rs.getString("departamento_nome"),
                    ativo = rs.getBoolean("ativo"),
                    version = rs.getLong("version").let { if (rs.wasNull()) null else it },
                    createdAt = instant(rs, "created_at"),
                    updatedAt = instant(rs, "updated_at"),
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
