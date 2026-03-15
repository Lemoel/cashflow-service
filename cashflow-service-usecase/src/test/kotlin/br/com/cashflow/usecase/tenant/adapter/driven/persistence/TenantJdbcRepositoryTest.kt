package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.model.TenantSchemaInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

class TenantJdbcRepositoryTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: TenantJdbcRepository

    @BeforeEach
    fun setUp() {
        repository = TenantJdbcRepository(jdbcTemplate)
    }

    @Test
    fun `findTenantSchemaByEmail returns TenantSchemaInfo when query returns one row`() {
        val tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val schemaName = "tenant_test"
        val info = TenantSchemaInfo(tenantId = tenantId, schemaName = schemaName)
        val sqlSlot = slot<String>()
        val emailSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(sqlSlot),
                any<RowMapper<TenantSchemaInfo>>(),
                capture(emailSlot),
            )
        } returns listOf(info)

        val result = repository.findTenantSchemaByEmail("user@example.com")

        assertThat(result).isNotNull
        assertThat(result!!.tenantId).isEqualTo(tenantId)
        assertThat(result.schemaName).isEqualTo(schemaName)
        assertThat(sqlSlot.captured).contains("core.user_tenant_map")
        assertThat(sqlSlot.captured).contains("core.tenants")
        assertThat(sqlSlot.captured).contains("WHERE m.email = ?")
        assertThat(emailSlot.captured).isEqualTo("user@example.com")
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<TenantSchemaInfo>>(), any<String>()) }
    }

    @Test
    fun `findTenantSchemaByEmail returns null when query returns empty list`() {
        val sqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(sqlSlot),
                any<RowMapper<TenantSchemaInfo>>(),
                any<String>(),
            )
        } returns emptyList()

        val result = repository.findTenantSchemaByEmail("unknown@example.com")

        assertThat(result).isNull()
        assertThat(sqlSlot.captured).contains("WHERE m.email = ?")
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<TenantSchemaInfo>>(), any<String>()) }
    }
}
