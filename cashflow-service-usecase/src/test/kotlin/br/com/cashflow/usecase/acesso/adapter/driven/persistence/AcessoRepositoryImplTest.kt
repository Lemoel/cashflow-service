package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

class AcessoRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: AcessoRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AcessoRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findTenantIdByEmail returns tenant id when query returns result`() {
        val email = "user@test.com"
        val tenantId = UUID.randomUUID()
        val sqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(sqlSlot),
                any<org.springframework.jdbc.core.RowMapper<UUID>>(),
                email,
            )
        } returns listOf(tenantId)

        val result = repository.findTenantIdByEmail(email)

        assertThat(result).isEqualTo(tenantId)
        assertThat(sqlSlot.captured).contains("SELECT c.tenant_id FROM eventos.acesso_congregacao ac")
        assertThat(sqlSlot.captured).contains("INNER JOIN eventos.congregacao c ON c.id = ac.congregacao_id")
        assertThat(sqlSlot.captured).contains("WHERE ac.email = ?")
        assertThat(sqlSlot.captured).contains("LIMIT 1")
        verify(exactly = 1) { jdbcTemplate.query(any(), any<org.springframework.jdbc.core.RowMapper<UUID>>(), email) }
    }

    @Test
    fun `findTenantIdByEmail returns null when query returns empty list`() {
        val email = "unknown@test.com"
        every { jdbcTemplate.query(any(), any<org.springframework.jdbc.core.RowMapper<UUID>>(), email) } returns emptyList<UUID>()

        val result = repository.findTenantIdByEmail(email)

        assertThat(result).isNull()
    }
}
