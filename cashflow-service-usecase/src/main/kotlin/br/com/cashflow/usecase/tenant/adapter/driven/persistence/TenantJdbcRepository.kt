package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.model.TenantSchemaInfo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
class TenantJdbcRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun findTenantSchemaByEmail(email: String): TenantSchemaInfo? =
        jdbcTemplate
            .query(
                FIND_TENANT_SCHEMA_BY_EMAIL_SQL,
                TENANT_SCHEMA_INFO_ROW_MAPPER,
                email,
            ).firstOrNull()

    companion object {
        private const val FIND_TENANT_SCHEMA_BY_EMAIL_SQL =
            """
            SELECT t.id, t.schema_name
            FROM core.user_tenant_map m
            INNER JOIN core.tenants t ON t.id = m.tenant_id
            WHERE m.email = ?
            """

        private val TENANT_SCHEMA_INFO_ROW_MAPPER =
            RowMapper<TenantSchemaInfo> { rs, _ ->
                TenantSchemaInfo(
                    tenantId = java.util.UUID.fromString(rs.getString("id")),
                    schemaName = rs.getString("schema_name")!!,
                )
            }
    }
}
