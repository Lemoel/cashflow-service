package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import org.springframework.data.domain.AuditorAware
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class DepartmentEntityCallback(
    private val auditorAware: AuditorAware<String>,
) : BeforeConvertCallback<Department> {
    override fun onBeforeConvert(department: Department): Department {
        val now = Instant.now()
        val auditor = auditorAware.currentAuditor.orElse("sistema")
        if (department.creationUserId.isBlank()) {
            department.creationUserId = auditor
        }
        department.modUserId = department.modUserId ?: auditor
        if (department.id == null) {
            department.id = UUID.randomUUID()
            department.createdAt = now
        }
        department.updatedAt = now
        return department
    }
}
