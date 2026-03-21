package br.com.cashflow.commons.tenant

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

class TenantSchemaCoroutineElement(
    private val schema: String?,
) : ThreadContextElement<String?> {
    companion object Key : CoroutineContext.Key<TenantSchemaCoroutineElement>

    override val key: CoroutineContext.Key<TenantSchemaCoroutineElement> get() = Key

    override fun updateThreadContext(context: CoroutineContext): String? {
        val previous = TenantContext.getSchema()
        if (schema == null) {
            TenantContext.clear()
        } else {
            TenantContext.setSchema(schema)
        }
        return previous
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: String?,
    ) {
        if (oldState == null) {
            TenantContext.clear()
        } else {
            TenantContext.setSchema(oldState)
        }
    }
}

fun tenantCoroutineContext(): CoroutineContext = TenantSchemaCoroutineElement(TenantContext.getSchema())
