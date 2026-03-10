package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import org.springframework.data.domain.AuditorAware
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class ParametroEntityCallback(
    private val auditorAware: AuditorAware<String>,
) : BeforeConvertCallback<Parametro> {
    override fun onBeforeConvert(parametro: Parametro): Parametro {
        val now = Instant.now()
        val auditor = auditorAware.currentAuditor.orElse("sistema")
        if (parametro.id == null) {
            parametro.id = UUID.randomUUID()
            parametro.creationUserId = auditor
            parametro.createdAt = now
        }
        parametro.modUserId = auditor
        parametro.updatedAt = now
        return parametro
    }
}
