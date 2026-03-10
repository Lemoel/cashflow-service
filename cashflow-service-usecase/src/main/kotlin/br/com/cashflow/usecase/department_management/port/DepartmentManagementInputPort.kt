package br.com.cashflow.usecase.department_management.port

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import br.com.cashflow.usecase.department.model.DepartmentPage
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentCreateRequest
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentUpdateRequest
import java.util.UUID

interface DepartmentManagementInputPort {
    fun create(
        tenantId: java.util.UUID,
        request: DepartmentCreateRequest,
    ): Department

    fun update(
        id: UUID,
        request: DepartmentUpdateRequest,
    ): Department

    fun findById(id: UUID): Department?

    fun findAll(
        filter: DepartmentFilter?,
        page: Int,
        size: Int,
    ): DepartmentPage

    fun findDepartmentsByCongregationId(congregationId: UUID): List<Department>

    fun delete(id: UUID)
}
