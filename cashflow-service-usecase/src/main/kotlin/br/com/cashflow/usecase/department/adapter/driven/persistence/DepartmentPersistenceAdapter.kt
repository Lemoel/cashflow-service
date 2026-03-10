package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import br.com.cashflow.usecase.department.model.DepartmentPage
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DepartmentPersistenceAdapter(
    private val departmentRepository: DepartmentRepository,
) : DepartmentOutputPort {
    override fun save(department: Department): Department = departmentRepository.save(department)

    override fun findById(id: UUID): Department? = departmentRepository.findById(id).orElse(null)

    override fun findAll(
        filter: DepartmentFilter?,
        page: Int,
        size: Int,
    ): DepartmentPage {
        val pageable = PageRequest.of(page, size)
        val springPage = departmentRepository.findFiltered(filter, pageable)
        return DepartmentPage(
            items = springPage.content,
            total = springPage.totalElements,
            page = springPage.number,
            pageSize = springPage.size,
        )
    }

    override fun findByTenantIdOrderByNomeAsc(tenantId: UUID): List<Department> = departmentRepository.findByTenantIdOrderByNomeAsc(tenantId)

    override fun deleteById(id: UUID) {
        departmentRepository.deleteById(id)
    }
}
