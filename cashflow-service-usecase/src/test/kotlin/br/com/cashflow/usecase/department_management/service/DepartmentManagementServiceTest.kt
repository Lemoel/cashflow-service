package br.com.cashflow.usecase.department_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import br.com.cashflow.usecase.department.model.DepartmentPage
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentCreateRequestDto
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentUpdateRequestDto
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

class DepartmentManagementServiceTest {
    private val departmentOutputPort: DepartmentOutputPort = mockk()
    private lateinit var service: DepartmentManagementService

    @BeforeEach
    fun setUp() {
        service = DepartmentManagementService(departmentOutputPort)
    }

    @Test
    fun `create returns saved department when valid`() {
        val tenantId = UUID.randomUUID()
        val request =
            DepartmentCreateRequestDto(
                nome = " Financeiro ",
                ativo = true,
            )
        val saved =
            Department(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                nome = "FINANCEIRO",
                ativo = true,
            )
        every {
            departmentOutputPort.save(match { it.nome == "FINANCEIRO" && it.tenantId == tenantId })
        } returns saved

        val result = service.create(tenantId, request)

        assertThat(result).isEqualTo(saved)
        assertThat(result.nome).isEqualTo("FINANCEIRO")
        verify(exactly = 1) { departmentOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws BusinessException when nome is blank`() {
        val tenantId = UUID.randomUUID()
        val request =
            DepartmentCreateRequestDto(
                nome = "   ",
                ativo = true,
            )

        assertThatThrownBy { service.create(tenantId, request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Nome do departamento é obrigatório")
        verify(exactly = 0) { departmentOutputPort.save(any()) }
    }

    @Test
    fun `create throws ConflictException when duplicate nome per tenant`() {
        val tenantId = UUID.randomUUID()
        val request =
            DepartmentCreateRequestDto(
                nome = "Financeiro",
                ativo = true,
            )
        every { departmentOutputPort.save(any()) } throws
            DataIntegrityViolationException("uk_departamento_tenant_nome")

        assertThatThrownBy { service.create(tenantId, request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um departamento com este nome nesta igreja")
    }

    @Test
    fun `update returns updated department when found`() {
        val id = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val existing =
            Department(
                id = id,
                tenantId = tenantId,
                nome = "OLD",
                ativo = true,
            )
        val request =
            DepartmentUpdateRequestDto(
                nome = " Novo Nome ",
                ativo = false,
            )
        every { departmentOutputPort.findById(id) } returns existing
        every { departmentOutputPort.save(match { it.nome == "NOVO NOME" && !it.ativo }) } answers
            { firstArg() }

        val result = service.update(id, request)

        assertThat(result.nome).isEqualTo("NOVO NOME")
        assertThat(result.ativo).isFalse()
        verify(exactly = 1) { departmentOutputPort.save(match { true }) }
    }

    @Test
    fun `update throws ResourceNotFoundException when department not found`() {
        val id = UUID.randomUUID()
        every { departmentOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                DepartmentUpdateRequestDto(nome = "Nome", ativo = true),
            )
        }.isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Departamento não encontrado")
        verify(exactly = 0) { departmentOutputPort.save(any()) }
    }

    @Test
    fun `update throws BusinessException when nome is blank`() {
        val id = UUID.randomUUID()
        val existing = Department(id = id, tenantId = UUID.randomUUID(), nome = "A", ativo = true)
        every { departmentOutputPort.findById(id) } returns existing

        assertThatThrownBy {
            service.update(id, DepartmentUpdateRequestDto(nome = "  ", ativo = true))
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Nome do departamento é obrigatório")
    }

    @Test
    fun `findById returns department when found`() {
        val id = UUID.randomUUID()
        val department = Department(id = id, tenantId = UUID.randomUUID(), nome = "A", ativo = true)
        every { departmentOutputPort.findById(id) } returns department

        val result = service.findById(id)

        assertThat(result).isEqualTo(department)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { departmentOutputPort.findById(id) } returns null

        val result = service.findById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `findAll returns empty page when filter tenantId is null`() {
        val result = service.findAll(DepartmentFilter(tenantId = null), 0, 10)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
        verify(exactly = 0) { departmentOutputPort.findAll(any(), any(), any()) }
    }

    @Test
    fun `findAll delegates to output port when filter has tenantId`() {
        val tenantId = UUID.randomUUID()
        val filter = DepartmentFilter(tenantId = tenantId, nome = "FIN", ativo = true)
        val page =
            DepartmentPage(
                listOf(Department(tenantId = tenantId, nome = "FINANCEIRO", ativo = true)),
                1L,
                0,
                10,
            )
        every { departmentOutputPort.findAll(match { it?.tenantId == tenantId }, 0, 10) } returns
            page

        val result = service.findAll(filter, 0, 10)

        assertThat(result).isEqualTo(page)
        verify(
            exactly = 1,
        ) { departmentOutputPort.findAll(match { it?.tenantId == tenantId }, 0, 10) }
    }

    @Test
    fun `delete throws ResourceNotFoundException when department not found`() {
        val id = UUID.randomUUID()
        every { departmentOutputPort.findById(id) } returns null

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Departamento não encontrado")
        verify(exactly = 0) { departmentOutputPort.deleteById(any()) }
    }

    @Test
    fun `delete calls deleteById when department exists`() {
        val id = UUID.randomUUID()
        val department = Department(id = id, tenantId = UUID.randomUUID(), nome = "A", ativo = true)
        every { departmentOutputPort.findById(id) } returns department
        every { departmentOutputPort.deleteById(id) } just runs

        service.delete(id)

        verify(exactly = 1) { departmentOutputPort.deleteById(id) }
    }

    @Test
    fun `delete throws ConflictException when FK violation on delete`() {
        val id = UUID.randomUUID()
        val department = Department(id = id, tenantId = UUID.randomUUID(), nome = "A", ativo = true)
        every { departmentOutputPort.findById(id) } returns department
        every { departmentOutputPort.deleteById(id) } throws
            DataIntegrityViolationException("fk_lancamento_departamento")

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("registros dependentes")
    }
}
