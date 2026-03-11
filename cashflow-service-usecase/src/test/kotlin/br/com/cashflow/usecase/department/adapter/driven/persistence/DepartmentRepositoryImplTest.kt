package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
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

class DepartmentRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: DepartmentRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = DepartmentRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findFiltered with null filter uses count and select without WHERE`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Department>>(), *anyVararg())
        } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0L)
        assertThat(countSqlSlot.captured).contains("SELECT COUNT(*) FROM eventos.departamento")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("SELECT * FROM eventos.departamento")
        assertThat(selectSqlSlot.captured).contains("ORDER BY d.nome ASC LIMIT ? OFFSET ?")
        verify(exactly = 1) { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) }
        verify(
            exactly = 1,
        ) { jdbcTemplate.query(any(), any<RowMapper<Department>>(), *anyVararg()) }
    }

    @Test
    fun `findFiltered with tenantId filter adds WHERE condition`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Department>>(), *anyVararg())
        } returns emptyList()

        val tenantId = UUID.randomUUID()
        val filter = DepartmentFilter(tenantId = tenantId, nome = null, ativo = null)
        val result = repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(result.totalElements).isEqualTo(0L)
        assertThat(countSqlSlot.captured).contains("d.tenant_id = ?")
        assertThat(selectSqlSlot.captured).contains("d.tenant_id = ?")
    }

    @Test
    fun `findFiltered with nome filter uses ILIKE condition`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            1L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Department>>(), *anyVararg())
        } returns emptyList()

        val filter = DepartmentFilter(tenantId = null, nome = "TI", ativo = null)
        val result = repository.findFiltered(filter, PageRequest.of(0, 5))

        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(countSqlSlot.captured).contains("d.nome ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("d.nome ILIKE ?")
    }

    @Test
    fun `findFiltered with ativo filter adds condition`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Department>>(), *anyVararg())
        } returns emptyList()

        val filter = DepartmentFilter(tenantId = null, nome = null, ativo = true)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(countSqlSlot.captured).contains("d.ativo = ?")
        assertThat(selectSqlSlot.captured).contains("d.ativo = ?")
    }

    @Test
    fun `findFiltered with all filters uses tenant nome and ativo conditions`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Department>>(), *anyVararg())
        } returns emptyList()

        val tenantId = UUID.randomUUID()
        val filter = DepartmentFilter(tenantId = tenantId, nome = "Vendas", ativo = false)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(countSqlSlot.captured).contains("d.tenant_id = ?")
        assertThat(countSqlSlot.captured).contains("d.nome ILIKE ?")
        assertThat(countSqlSlot.captured).contains("d.ativo = ?")
        assertThat(selectSqlSlot.captured).contains("d.tenant_id = ?")
        assertThat(selectSqlSlot.captured).contains("d.nome ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("d.ativo = ?")
    }

    @Test
    fun `findFiltered with blank nome does not add nome condition`() {
        val countSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every { jdbcTemplate.query(any(), any<RowMapper<Department>>(), *anyVararg()) } returns
            emptyList()

        val filter = DepartmentFilter(tenantId = null, nome = "  ", ativo = null)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(countSqlSlot.captured).doesNotContain("nome")
    }

    @Test
    fun `findFiltered returns PageImpl with content from query`() {
        val department =
            Department(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                nome = "TI",
                ativo = true,
            )
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(any(), any<RowMapper<Department>>(), *anyVararg()) } returns
            listOf(department)

        val result = repository.findFiltered(null, PageRequest.of(0, 10))

        assertThat(result).isInstanceOf(PageImpl::class.java)
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].nome).isEqualTo("TI")
        assertThat(result.totalElements).isEqualTo(1L)
    }
}
