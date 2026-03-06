package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.port.DepartmentFilter
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class DepartmentPersistenceAdapterTest {
    private val departmentRepository: DepartmentRepository = mockk()
    private lateinit var adapter: DepartmentPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = DepartmentPersistenceAdapter(departmentRepository)
    }

    @Test
    fun `save delegates to repository and returns saved department`() {
        val department =
            Department(
                id = null,
                tenantId = UUID.randomUUID(),
                nome = "FINANCEIRO",
                ativo = true,
            )
        val saved =
            Department(
                id = UUID.randomUUID(),
                tenantId = department.tenantId,
                nome = "FINANCEIRO",
                ativo = true,
            )
        every { departmentRepository.save(department) } returns saved

        val result = adapter.save(department)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { departmentRepository.save(department) }
    }

    @Test
    fun `findById delegates to repository and returns entity when found`() {
        val id = UUID.randomUUID()
        val department =
            Department(
                id = id,
                tenantId = UUID.randomUUID(),
                nome = "FINANCEIRO",
                ativo = true,
            )
        every { departmentRepository.findById(id) } returns Optional.of(department)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(department)
        verify(exactly = 1) { departmentRepository.findById(id) }
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { departmentRepository.findById(id) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { departmentRepository.findById(id) }
    }

    @Test
    fun `findAll delegates to findFiltered and returns DepartmentPage`() {
        val pageable = PageRequest.of(0, 10)
        val tenantId = UUID.randomUUID()
        val departments =
            listOf(
                Department(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    nome = "FINANCEIRO",
                    ativo = true,
                ),
            )
        val springPage = PageImpl(departments, pageable, 1L)
        val filter = DepartmentFilter(tenantId = tenantId)
        every { departmentRepository.findFiltered(filter, pageable) } returns springPage

        val result = adapter.findAll(filter, 0, 10)

        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
        verify(exactly = 1) { departmentRepository.findFiltered(filter, pageable) }
    }

    @Test
    fun `findByTenantIdOrderByNomeAsc delegates to repository`() {
        val tenantId = UUID.randomUUID()
        val departments =
            listOf(
                Department(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    nome = "A",
                    ativo = true,
                ),
            )
        every { departmentRepository.findByTenantIdOrderByNomeAsc(tenantId) } returns departments

        val result = adapter.findByTenantIdOrderByNomeAsc(tenantId)

        assertThat(result).isEqualTo(departments)
        verify(exactly = 1) { departmentRepository.findByTenantIdOrderByNomeAsc(tenantId) }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { departmentRepository.deleteById(id) } just runs

        adapter.deleteById(id)

        verify(exactly = 1) { departmentRepository.deleteById(id) }
    }
}
