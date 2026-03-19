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
) : DepartmentManagementInputPort {
    override fun create(
        tenantId: UUID,
        request: DepartmentCreateRequestDto,
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
        request: DepartmentUpdateRequestDto,
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
