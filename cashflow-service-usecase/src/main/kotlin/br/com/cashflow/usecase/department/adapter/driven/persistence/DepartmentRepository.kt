package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface DepartmentRepository :
    JpaRepository<Department, UUID>,
    JpaSpecificationExecutor<Department> {
    fun findFiltered(
        filter: DepartmentFilter?,
        pageable: Pageable,
    ): Page<Department> {
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Order.asc("nome"))
        val p = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        return findAll(DepartmentSpecification.fromFilter(filter), p)
    }

    fun findByTenantIdOrderByNomeAsc(tenantId: UUID): List<Department>
}
