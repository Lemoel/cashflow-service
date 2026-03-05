package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import org.springframework.data.domain.AuditorAware
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class TenantEntityCallback(
    private val auditorAware: AuditorAware<String>,
) : BeforeConvertCallback<Tenant> {

    override fun onBeforeConvert(tenant: Tenant): Tenant {
        val now = Instant.now()
        val auditor = auditorAware.currentAuditor.orElse("system")
        if (tenant.id == null) {
            tenant.id = UUID.randomUUID()
            tenant.creationUserId = auditor
            tenant.createdAt = now
        }
        tenant.modUserId = auditor
        tenant.updatedAt = now
        return tenant
    }
}
