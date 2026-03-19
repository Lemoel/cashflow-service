package br.com.cashflow.usecase.tenant_management.port

interface TenantSchemaProvisionerPort {
    fun provision(schemaName: String)

    fun dropSchema(schemaName: String)
}
