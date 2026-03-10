package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

class TenantRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: TenantRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = TenantRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findFiltered with null filter uses count and select without WHERE`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 0L
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Tenant>>(), *anyVararg()) } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0L)
        assertThat(countSqlSlot.captured).contains("SELECT COUNT(*) FROM core.tenants")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("SELECT * FROM core.tenants")
        assertThat(selectSqlSlot.captured).contains("ORDER BY nome_fantasia ASC LIMIT ? OFFSET ?")
        verify(exactly = 1) { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) }
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<Tenant>>(), *anyVararg()) }
    }

    @Test
    fun `findFiltered with nome filter uses ILIKE condition`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Tenant>>(), *anyVararg()) } returns emptyList()

        val pageable = PageRequest.of(0, 5)
        val filter = TenantFilter(nome = "Church", cnpj = null, active = null)
        val result = repository.findFiltered(filter, pageable)

        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(countSqlSlot.captured).contains("nome_fantasia ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("nome_fantasia ILIKE ?")
    }

    @Test
    fun `findFiltered with all filters uses nome cnpj and ativo conditions`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 0L
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Tenant>>(), *anyVararg()) } returns emptyList()

        val filter = TenantFilter(nome = "A", cnpj = "12345678000199", active = true)
        val result = repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(countSqlSlot.captured).contains("nome_fantasia ILIKE ?")
        assertThat(countSqlSlot.captured).contains("cnpj = ?")
        assertThat(countSqlSlot.captured).contains("ativo = ?")
        assertThat(selectSqlSlot.captured).contains("nome_fantasia ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("cnpj = ?")
        assertThat(selectSqlSlot.captured).contains("ativo = ?")
    }

    @Test
    fun `findFiltered returns PageImpl with content from query`() {
        val tenant =
            Tenant(id = UUID.randomUUID(), cnpj = "1", tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(any(), any<RowMapper<Tenant>>(), *anyVararg()) } returns listOf(tenant)

        val result = repository.findFiltered(null, PageRequest.of(0, 10))

        assertThat(result).isInstanceOf(PageImpl::class.java)
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].tradeName).isEqualTo("T")
        assertThat(result.totalElements).isEqualTo(1L)
    }
}
