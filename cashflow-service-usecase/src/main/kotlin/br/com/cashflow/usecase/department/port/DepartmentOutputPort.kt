package br.com.cashflow.usecase.department.port

import br.com.cashflow.usecase.department.entity.Department
import java.util.UUID

interface DepartmentOutputPort {
    fun save(department: Department): Department

    fun findById(id: UUID): Department?

    fun findAll(
        filter: DepartmentFilter?,
        page: Int,
        size: Int,
    ): DepartmentPage

    fun findByTenantIdOrderByNomeAsc(tenantId: UUID): List<Department>

    fun deleteById(id: UUID)
}
