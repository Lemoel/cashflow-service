package br.com.cashflow.app.tenant

import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantSchemaResolver(
    private val tenantOutputPort: TenantOutputPort,
) {
    fun resolve(tenantId: UUID): String? = tenantOutputPort.findById(tenantId)?.schemaName
}
