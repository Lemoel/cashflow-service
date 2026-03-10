package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface DepartmentRepository :
    CrudRepository<Department, UUID>,
    DepartmentRepositoryCustom {
    fun findByTenantIdOrderByNomeAsc(tenantId: UUID): List<Department>
}

interface DepartmentRepositoryCustom {
    fun findFiltered(
        filter: DepartmentFilter?,
        pageable: Pageable,
    ): Page<Department>
}
