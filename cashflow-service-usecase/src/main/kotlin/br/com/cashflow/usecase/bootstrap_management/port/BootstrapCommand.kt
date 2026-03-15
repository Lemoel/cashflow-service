package br.com.cashflow.usecase.bootstrap_management.port

data class BootstrapTenantData(
    val tradeName: String,
    val companyName: String?,
    val cnpj: String,
    val street: String,
    val number: String,
    val complement: String?,
    val neighborhood: String?,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String?,
    val email: String?,
    val active: Boolean = true,
)

data class BootstrapAdminUserData(
    val nome: String,
    val email: String,
)

data class BootstrapCongregationData(
    val nome: String,
    val cnpj: String?,
    val logradouro: String,
    val bairro: String,
    val numero: String,
    val cidade: String,
    val uf: String,
    val cep: String,
    val email: String?,
    val telefone: String?,
    val ativo: Boolean = true,
)

data class BootstrapCommand(
    val tenant: BootstrapTenantData,
    val adminUser: BootstrapAdminUserData,
    val firstCongregation: BootstrapCongregationData,
)
