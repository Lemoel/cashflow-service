package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import org.springframework.data.domain.AuditorAware
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class CongregationEntityCallback(
    private val auditorAware: AuditorAware<String>,
) : BeforeConvertCallback<Congregation> {
    override fun onBeforeConvert(congregation: Congregation): Congregation {
        val now = Instant.now()
        val auditor = auditorAware.currentAuditor.orElse("sistema")
        if (congregation.id == null) {
            congregation.id = UUID.randomUUID()
            congregation.creationUserId = auditor
            congregation.createdAt = now
        }
        congregation.modUserId = auditor
        congregation.updatedAt = now
        return congregation
    }
}
