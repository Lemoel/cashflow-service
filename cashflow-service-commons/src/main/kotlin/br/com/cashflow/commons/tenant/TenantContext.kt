package br.com.cashflow.commons.tenant

object TenantContext {
    private val currentSchema = ThreadLocal<String?>()

    fun setSchema(schema: String) {
        currentSchema.set(schema)
    }

    fun getSchema(): String? = currentSchema.get()

    fun clear() {
        currentSchema.remove()
    }

    fun requireSchema(): String = currentSchema.get() ?: throw IllegalStateException("Tenant schema is not set")
}
