package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate

private data class WherePart(
    val clause: String,
    val params: List<Any>,
)

class AcessoRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : AcessoRepositoryCustom {
    override fun findFiltered(
        filter: AcessoFilter?,
        pageable: Pageable,
    ): List<AcessoListItem> {
        val where = buildWhere(filter)
        val sql =
            """
            SELECT a.email, a.nome, a.telefone, a.tipo_acesso, a.ativo, a.data, a.mod_date_time,
                   c.id AS congregacao_id, c.nome AS congregacao_nome
            FROM acesso a
            LEFT JOIN acesso_congregacao ac ON a.email = ac.email
            LEFT JOIN congregacao c ON c.id = ac.congregacao_id
            ${where.clause}
            ORDER BY a.nome ASC
            LIMIT ? OFFSET ?
            """.trimIndent()
        val pageParams = where.params + pageable.pageSize + pageable.offset
        return jdbcTemplate.query(sql, AcessoListItemRowMapper(), *pageParams.toTypedArray())
    }

    override fun countFiltered(filter: AcessoFilter?): Long {
        val where = buildWhere(filter)
        val sql =
            """
            SELECT COUNT(DISTINCT a.email)
            FROM acesso a
            LEFT JOIN acesso_congregacao ac ON a.email = ac.email
            ${where.clause}
            """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Long::class.java, *where.params.toTypedArray()) ?: 0L
    }

    private fun buildWhere(filter: AcessoFilter?): WherePart {
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
        val clause = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        return WherePart(clause = clause, params = params)
    }
}
