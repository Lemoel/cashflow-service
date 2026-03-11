package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class DepartmentRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : DepartmentRepositoryCustom {
    override fun findFiltered(
        filter: DepartmentFilter?,
        pageable: Pageable,
    ): Page<Department> {
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()
        filter?.let { f ->

            f.tenantId?.let {
                conditions.add("d.tenant_id = ?")
                params.add(it)
            }

            f.nome?.takeIf { it.isNotBlank() }?.let {
                conditions.add("d.nome ILIKE ?")
                params.add("%$it%")
            }

            f.ativo?.let {
                conditions.add("d.ativo = ?")
                params.add(it)
            }
        }
        val whereClause =
            if (conditions.isEmpty()) {
                ""
            } else {
                " WHERE " +
                    conditions.joinToString(" AND ")
            }
        val countSql = "SELECT COUNT(*) FROM eventos.departamento d$whereClause"
        val total =
            jdbcTemplate.queryForObject(countSql, Long::class.java, *params.toTypedArray()) ?: 0L
        params.add(pageable.pageSize)
        params.add(pageable.offset)
        val selectSql = "SELECT * FROM eventos.departamento d$whereClause ORDER BY d.nome ASC LIMIT ? OFFSET ?"
        val items = jdbcTemplate.query(selectSql, DEPARTMENT_ROW_MAPPER, *params.toTypedArray())
        return PageImpl(items, pageable, total)
    }

    companion object {
        private val DEPARTMENT_ROW_MAPPER =
            RowMapper<Department> { rs: ResultSet, _: Int ->
                Department(
                    id = uuid(rs, "id"),
                    tenantId = uuid(rs, "tenant_id"),
                    nome = rs.getString("nome") ?: "",
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
