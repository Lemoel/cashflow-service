package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

class AcessoRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : AcessoRepositoryCustom {
    override fun findTenantIdByEmail(email: String): UUID? {
        val sql =
            """
            SELECT c.tenant_id FROM eventos.acesso_congregacao ac
            INNER JOIN eventos.congregacao c ON c.id = ac.congregacao_id
            WHERE ac.email = ?
            LIMIT 1
            """.trimIndent()
        val result = jdbcTemplate.query(sql, { rs, _ -> rs.getObject("tenant_id", UUID::class.java) }, email)
        return result.firstOrNull()
    }
}
