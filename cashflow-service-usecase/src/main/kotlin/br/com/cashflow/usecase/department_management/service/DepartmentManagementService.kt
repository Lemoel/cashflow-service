package br.com.cashflow.usecase.department_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.port.DepartmentFilter
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.department.port.DepartmentPage
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentCreateRequest
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentUpdateRequest
import br.com.cashflow.usecase.department_management.adapter.external.dto.applyTo
import br.com.cashflow.usecase.department_management.adapter.external.dto.toEntity
import br.com.cashflow.usecase.department_management.port.DepartmentManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DepartmentManagementService(
    private val departmentOutputPort: DepartmentOutputPort,
    private val congregationOutputPort: CongregationOutputPort,
) : DepartmentManagementInputPort {
    override fun create(
        tenantId: UUID,
        request: DepartmentCreateRequest,
    ): Department {
        if (request.nome.isBlank()) {
            throw BusinessException("Nome do departamento é obrigatório.")
        }
        val entity = request.toEntity(tenantId)
        return try {
            departmentOutputPort.save(entity)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException("Já existe um departamento com este nome nesta igreja.")
        }
    }

    override fun update(
        id: UUID,
        request: DepartmentUpdateRequest,
    ): Department {
        val existing =
            departmentOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Departamento não encontrado")
        if (request.nome.isBlank()) {
            throw BusinessException("Nome do departamento é obrigatório.")
        }
        request.applyTo(existing)
        return try {
            departmentOutputPort.save(existing)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException("Já existe um departamento com este nome nesta igreja.")
        }
    }

    override fun findById(id: UUID): Department? = departmentOutputPort.findById(id)

    override fun findAll(
        filter: DepartmentFilter?,
        page: Int,
        size: Int,
    ): DepartmentPage {
        if (filter?.tenantId == null) {
            return DepartmentPage(items = emptyList(), total = 0L, page = page, pageSize = size)
        }

        val normalizedFilter =
            filter.copy(
                nome =
                    filter.nome
                        ?.trim()
                        ?.uppercase()
                        ?.takeIf { it.isNotBlank() },
            )
        return departmentOutputPort.findAll(normalizedFilter, page, size)
    }

    override fun findDepartmentsByCongregationId(congregationId: UUID): List<Department> {
        val congregation = congregationOutputPort.findById(congregationId) ?: return emptyList()
        val tenantId = congregation.tenantId ?: return emptyList()
        return departmentOutputPort.findByTenantIdOrderByNomeAsc(tenantId)
    }

    @Transactional
    override fun delete(id: UUID) {
        val existing =
            departmentOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Departamento não encontrado")
        try {
            departmentOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Não é possível excluir o departamento. Existem registros dependentes (máquinas ou lançamentos).",
            )
        }
    }
}
