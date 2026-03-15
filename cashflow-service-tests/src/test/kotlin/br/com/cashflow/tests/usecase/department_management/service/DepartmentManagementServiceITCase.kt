package br.com.cashflow.tests.usecase.department_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.tests.config.TestTenantConfig
import br.com.cashflow.usecase.department.model.DepartmentFilter
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentCreateRequestDto
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentUpdateRequestDto
import br.com.cashflow.usecase.department_management.port.DepartmentManagementInputPort
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@SqlSetUp(value = ["/db/scripts/department/load.sql"])
@SqlTearDown(value = ["/db/scripts/department/teardown.sql"])
class DepartmentManagementServiceITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var departmentManagement: DepartmentManagementInputPort

    @Autowired
    private lateinit var tenantManagement: TenantManagementInputPort

    private fun createTenant(): UUID = TestTenantConfig.TEST_TENANT_ID

    @Test
    fun should_CreateFindUpdateFindAllAndDelete_When_FullCrud() {
        val tenantId = createTenant()
        val createRequest =
            DepartmentCreateRequestDto(
                nome = "Departamento CRUD",
                ativo = true,
            )

        val created = departmentManagement.create(tenantId, createRequest)

        assertThat(created.id).isNotNull()
        assertThat(created.tenantId).isEqualTo(tenantId)
        assertThat(created.nome).isEqualTo("DEPARTAMENTO CRUD")
        assertThat(created.ativo).isTrue()

        val found = departmentManagement.findById(created.id!!)
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.nome).isEqualTo("DEPARTAMENTO CRUD")

        val updateRequest =
            DepartmentUpdateRequestDto(
                nome = "Departamento CRUD Atualizado",
                ativo = false,
            )
        val updated = departmentManagement.update(created.id!!, updateRequest)
        assertThat(updated.nome).isEqualTo("DEPARTAMENTO CRUD ATUALIZADO")
        assertThat(updated.ativo).isFalse()

        val page = departmentManagement.findAll(DepartmentFilter(tenantId = tenantId), 0, 10)
        assertThat(page.items).isNotEmpty
        assertThat(page.total).isGreaterThanOrEqualTo(1)

        departmentManagement.delete(created.id!!)
        val afterDelete = departmentManagement.findById(created.id!!)
        assertThat(afterDelete).isNull()
    }

    @Test
    fun should_ThrowConflictException_When_CreateWithDuplicateNomeInSameTenant() {
        val tenantId = createTenant()
        val request = DepartmentCreateRequestDto(nome = "TI", ativo = true)
        departmentManagement.create(tenantId, request)

        assertThatThrownBy { departmentManagement.create(tenantId, request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um departamento com este nome")
    }

    @Test
    fun should_ReturnEmptyPage_When_FindAllWithNullTenantIdInFilter() {
        val page = departmentManagement.findAll(DepartmentFilter(tenantId = null), 0, 10)
        assertThat(page.items).isEmpty()
        assertThat(page.total).isEqualTo(0L)
    }

    @Test
    fun should_ReturnNull_When_FindByIdAndDepartmentNotFound() {
        val result = departmentManagement.findById(UUID.randomUUID())
        assertThat(result).isNull()
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_UpdateAndDepartmentNotFound() {
        val request = DepartmentUpdateRequestDto(nome = "Qualquer", ativo = true)
        assertThatThrownBy { departmentManagement.update(UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Departamento não encontrado")
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_DeleteAndDepartmentNotFound() {
        assertThatThrownBy { departmentManagement.delete(UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Departamento não encontrado")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateWithBlankNome() {
        val tenantId = createTenant()
        val request = DepartmentCreateRequestDto(nome = "   ", ativo = true)
        assertThatThrownBy { departmentManagement.create(tenantId, request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Nome do departamento é obrigatório")
    }

    @Test
    fun should_ReturnMatchingDepartments_When_FindAllWithFilterByNome() {
        val tenantId = createTenant()
        departmentManagement.create(
            tenantId,
            DepartmentCreateRequestDto(nome = "Vendas", ativo = true),
        )

        val page =
            departmentManagement.findAll(
                DepartmentFilter(tenantId = tenantId, nome = "vendas"),
                0,
                10,
            )
        assertThat(page.items).isNotEmpty
        assertThat(page.items.any { it.nome.contains("VENDAS") }).isTrue()
    }
}
