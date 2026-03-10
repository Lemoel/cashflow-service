package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class ParametroRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ParametroRepositoryCustom {
    override fun findWithFilters(
        filter: ParametroFilter?,
        pageable: Pageable,
    ): Page<Parametro> {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        filter?.let { f ->
            f.chave?.takeIf { it.isNotBlank() }?.let {
                conditions.add("p.chave ILIKE ?")
                params.add("%$it%")
            }
            f.ativo?.let {
                conditions.add("p.ativo = ?")
                params.add(it)
            }
        }
        val whereClause = if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        val countSql = "SELECT COUNT(*) FROM eventos.parametro p$whereClause"
        val total = jdbcTemplate.queryForObject(countSql, Long::class.java, *params.toTypedArray()) ?: 0L
        params.add(pageable.pageSize)
        params.add(pageable.offset)
        val selectSql = "SELECT * FROM eventos.parametro p$whereClause ORDER BY p.chave ASC LIMIT ? OFFSET ?"
        val items = jdbcTemplate.query(selectSql, PARAMETRO_ROW_MAPPER, *params.toTypedArray())
        return PageImpl(items, pageable, total)
    }

    companion object {
        private val PARAMETRO_ROW_MAPPER =
            RowMapper<Parametro> { rs: ResultSet, _: Int ->
                Parametro(
                    id = uuid(rs, "id"),
                    chave = rs.getString("chave") ?: "",
                    valorTexto = rs.getString("valor_texto"),
                    valorInteiro = rs.getObject("valor_inteiro")?.let { (it as Number).toLong() },
                    valorDecimal = rs.getObject("valor_decimal")?.let { (it as Number).toDouble() },
                    tipo = rs.getString("tipo") ?: "",
                    ativo = rs.getBoolean("ativo"),
                    creationUserId = rs.getString("creation_user_id") ?: "",
                    modUserId = rs.getString("mod_user_id"),
                    createdAt = timestamp(rs, "created_at"),
                    updatedAt = timestamp(rs, "updated_at"),
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
