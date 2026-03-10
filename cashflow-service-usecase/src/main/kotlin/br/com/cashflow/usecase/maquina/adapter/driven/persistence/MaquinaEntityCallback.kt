package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.entity.Maquina
import org.springframework.data.domain.AuditorAware
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MaquinaEntityCallback(
    private val auditorAware: AuditorAware<String>,
) : BeforeConvertCallback<Maquina> {
    override fun onBeforeConvert(maquina: Maquina): Maquina {
        val now = Instant.now()

        val auditor = auditorAware.currentAuditor.orElse("sistema")

        if (maquina.creationUserId.isBlank()) {
            maquina.creationUserId = auditor
        }

        maquina.modUserId = auditor

        if (maquina.id == null) {
            maquina.id = UUID.randomUUID()
            maquina.createdAt = now
        }

        maquina.updatedAt = now

        return maquina
    }
}
