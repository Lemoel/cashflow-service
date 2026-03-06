package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class CongregationRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : CongregationRepositoryCustom {
    override fun findFiltered(
        filter: CongregationFilter?,
        pageable: Pageable,
    ): Page<Congregation> {

        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        filter?.let { f ->
            f.nome?.takeIf { it.isNotBlank() }?.let {
                conditions.add("c.nome = ?")
                params.add(it)
            }

            f.cnpj?.takeIf { it.isNotBlank() }?.let {
                conditions.add("c.cnpj = ?")
                params.add(it)
            }

            f.ativo?.let {
                conditions.add("c.ativo = ?")
                params.add(it)
            }

        }
        val whereClause = if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        val countSql = "SELECT COUNT(*) FROM eventos.congregacao c$whereClause"
        val total = jdbcTemplate.queryForObject(countSql, Long::class.java, *params.toTypedArray()) ?: 0L
        params.add(pageable.pageSize)
        params.add(pageable.offset)
        val selectSql = "SELECT * FROM eventos.congregacao c$whereClause ORDER BY c.nome ASC LIMIT ? OFFSET ?"
        val items = jdbcTemplate.query(selectSql, CONGREGATION_ROW_MAPPER, *params.toTypedArray())
        return PageImpl(items, pageable, total)
    }

    companion object {
        private val CONGREGATION_ROW_MAPPER =
            RowMapper<Congregation> { rs: ResultSet, _: Int ->
                Congregation(
                    id = uuid(rs, "id"),
                    tenantId = uuid(rs, "tenant_id"),
                    setorialId = uuid(rs, "setorial_id"),
                    nome = rs.getString("nome") ?: "",
                    cnpj = rs.getString("cnpj"),
                    logradouro = rs.getString("logradouro") ?: "",
                    bairro = rs.getString("bairro") ?: "",
                    numero = rs.getString("numero") ?: "",
                    cidade = rs.getString("cidade") ?: "",
                    uf = rs.getString("uf") ?: "",
                    cep = rs.getString("cep") ?: "",
                    email = rs.getString("email"),
                    telefone = rs.getString("telefone"),
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
