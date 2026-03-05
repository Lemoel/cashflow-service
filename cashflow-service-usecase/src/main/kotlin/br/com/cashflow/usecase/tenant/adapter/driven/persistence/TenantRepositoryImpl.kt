package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class TenantRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : TenantRepositoryCustom {
    override fun findFiltered(
        filter: TenantFilter?,
        pageable: Pageable,
    ): Page<Tenant> {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()
        filter?.let { f ->
            f.nome?.takeIf { it.isNotBlank() }?.let {
                conditions.add("nome_fantasia ILIKE ?")
                params.add("%$it%")
            }
            f.cnpj?.takeIf { it.isNotBlank() }?.let {
                conditions.add("cnpj = ?")
                params.add(it)
            }
            f.active?.let {
                conditions.add("ativo = ?")
                params.add(it)
            }
        }
        val whereClause = if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        val countSql = "SELECT COUNT(*) FROM core.tenants$whereClause"
        val total = jdbcTemplate.queryForObject(countSql, Long::class.java, *params.toTypedArray()) ?: 0L
        params.add(pageable.pageSize)
        params.add(pageable.offset)
        val selectSql = "SELECT * FROM core.tenants$whereClause ORDER BY nome_fantasia ASC LIMIT ? OFFSET ?"
        val items = jdbcTemplate.query(selectSql, TENANT_ROW_MAPPER, *params.toTypedArray())
        return PageImpl(items, pageable, total)
    }

    companion object {
        private val TENANT_ROW_MAPPER =
            RowMapper<Tenant> { rs: ResultSet, _: Int ->
                Tenant(
                    id = uuid(rs, "id"),
                    cnpj = rs.getString("cnpj") ?: "",
                    tradeName = rs.getString("nome_fantasia") ?: "",
                    companyName = rs.getString("razao_social"),
                    street = rs.getString("logradouro") ?: "",
                    number = rs.getString("numero") ?: "",
                    complement = rs.getString("complemento"),
                    neighborhood = rs.getString("bairro"),
                    city = rs.getString("cidade") ?: "",
                    state = rs.getString("uf") ?: "",
                    zipCode = rs.getString("cep") ?: "",
                    phone = rs.getString("telefone"),
                    email = rs.getString("email"),
                    active = rs.getBoolean("ativo"),
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
