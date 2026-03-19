package br.com.cashflow.usecase.bootstrap_management.adapter.external.dto

import br.com.cashflow.usecase.bootstrap_management.port.BootstrapResult

data class BootstrapResponseDto(
    val tenantId: String,
    val tenantSchemaName: String,
    val adminEmail: String,
    val temporaryPassword: String,
    val congregationId: String,
    val _links: Map<String, BootstrapLinkDto>,
)

data class BootstrapLinkDto(
    val href: String,
    val method: String? = "GET",
)

fun BootstrapResult.toResponseDto(baseUrl: String): BootstrapResponseDto {
    val tenantsPath = "$baseUrl/api/v1/tenants"
    val tenantUrl = "$tenantsPath/$tenantId"

    return BootstrapResponseDto(
        tenantId = tenantId.toString(),
        tenantSchemaName = tenantSchemaName,
        adminEmail = adminEmail,
        temporaryPassword = temporaryPassword,
        congregationId = congregationId.toString(),
        _links =
            mapOf(
                "self" to BootstrapLinkDto(href = tenantUrl),
                "tenant" to BootstrapLinkDto(href = tenantUrl),
                "login" to BootstrapLinkDto(href = "$baseUrl/api/v1/auth/login", method = "POST"),
            ),
    )
}
