package br.com.cashflow.usecase.movimento_api.adapter.driven.persistence

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MovimentoApiEntityCallback : BeforeConvertCallback<MovimentoApi> {
    override fun onBeforeConvert(movimento: MovimentoApi): MovimentoApi {
        val now = Instant.now()
        if (movimento.id == null) {
            movimento.id = UUID.randomUUID()
            movimento.createdAt = now
        }
        movimento.updatedAt = now
        return movimento
    }
}
