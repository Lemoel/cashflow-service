package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class AcessoRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : AcessoRepositoryCustom {
    override fun findFiltered(
        filter: AcessoFilter?,
        pageable: Pageable,
    ): List<AcessoListItem> {
        val (whereClause, params) = buildWhereAndParams(filter)
        val sql =
            """
            SELECT a.email, a.nome, a.telefone, a.tipo_acesso, a.ativo, a.data, a.mod_date_time,
                   c.id AS congregacao_id, c.nome AS congregacao_nome
            FROM eventos.acesso a
            LEFT JOIN eventos.acesso_congregacao ac ON a.email = ac.email
            LEFT JOIN eventos.congregacao c ON c.id = ac.congregacao_id
            $whereClause
            ORDER BY a.nome ASC
            LIMIT ? OFFSET ?
            """.trimIndent()
        val pageParams = params + pageable.pageSize + pageable.offset
        return jdbcTemplate.query(sql, ACESSO_LIST_ITEM_ROW_MAPPER, *pageParams.toTypedArray())
    }

    override fun countFiltered(filter: AcessoFilter?): Long {
        val (whereClause, params) = buildWhereAndParams(filter)
        val sql =
            """
            SELECT COUNT(DISTINCT a.email)
            FROM eventos.acesso a
            LEFT JOIN eventos.acesso_congregacao ac ON a.email = ac.email
            $whereClause
            """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Long::class.java, *params.toTypedArray()) ?: 0L
    }

    private fun buildWhereAndParams(filter: AcessoFilter?): Pair<String, List<Any>> {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()
        filter?.let { f ->
            f.email?.takeIf { it.isNotBlank() }?.let {
                conditions.add("a.email ILIKE ?")
                params.add("%$it%")
            }
            f.congregacaoId?.let {
                conditions.add("ac.congregacao_id = ?")
                params.add(it)
            }
            f.perfil?.takeIf { it.isNotBlank() }?.let {
                conditions.add("a.tipo_acesso = ?")
                params.add(it)
            }
            f.ativo?.let {
                conditions.add("a.ativo = ?")
                params.add(it)
            }
        }
        val whereClause =
            if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        return Pair(whereClause, params)
    }

    companion object {
        private val ACESSO_LIST_ITEM_ROW_MAPPER =
            RowMapper<AcessoListItem> { rs: ResultSet, _: Int ->
                AcessoListItem(
                    email = rs.getString("email") ?: "",
                    nome = rs.getString("nome"),
                    telefone = rs.getString("telefone"),
                    tipoAcesso = rs.getString("tipo_acesso") ?: "",
                    ativo = rs.getBoolean("ativo"),
                    data = timestamp(rs, "data"),
                    modDateTime = timestamp(rs, "mod_date_time"),
                    congregacaoId = uuid(rs, "congregacao_id"),
                    congregacaoNome = rs.getString("congregacao_nome"),
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

        private fun timestamp(
            rs: ResultSet,
            column: String,
        ): Instant? {
            val ts = rs.getTimestamp(column) ?: return null
            return ts.toInstant()
        }
    }
}
