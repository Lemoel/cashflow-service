package br.com.cashflow.usecase.tenant_management.adapter.external.dto

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantIdName

private fun normalizeCnpj(value: String): String = value.filter { it.isDigit() }

fun TenantCreateRequestDto.toEntity(): Tenant {
    val digitsOnly = normalizeCnpj(cnpj)
    return Tenant(
        cnpj = digitsOnly,
        tradeName = tradeName.trim().uppercase(),
        companyName = companyName?.trim()?.uppercase(),
        street = street.trim().uppercase(),
        number = number.trim(),
        complement = complement?.trim()?.uppercase(),
        neighborhood = neighborhood?.trim()?.uppercase(),
        city = city.trim().uppercase(),
        state = state.trim().uppercase(),
        zipCode = zipCode.trim(),
        phone = phone?.trim(),
        email = email?.trim()?.lowercase(),
        active = active,
    )
}

fun TenantUpdateRequestDto.applyTo(tenant: Tenant) {
    tenant.tradeName = tradeName.trim().uppercase()
    tenant.companyName = companyName?.trim()?.uppercase()
    tenant.street = street.trim().uppercase()
    tenant.number = number.trim()
    tenant.complement = complement?.trim()?.uppercase()
    tenant.neighborhood = neighborhood?.trim()?.uppercase()
    tenant.city = city.trim().uppercase()
    tenant.state = state.trim().uppercase()
    tenant.zipCode = zipCode.trim()
    tenant.phone = phone?.trim()
    tenant.email = email?.trim()?.lowercase()
    tenant.active = active
}

data class TenantResponseDto(
    val id: String,
    val cnpj: String,
    val tradeName: String,
    val companyName: String?,
    val street: String,
    val number: String,
    val complement: String?,
    val neighborhood: String?,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String?,
    val email: String?,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String?,
)

data class TenantListOption(
    val id: String,
    val name: String,
)

data class TenantListResponse(
    val items: List<TenantResponseDto>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

fun Tenant.toResponse(): TenantResponseDto =
    TenantResponseDto(
        id = requireNotNull(id) { "Tenant id must not be null" }.toString(),
        cnpj = cnpj,
        tradeName = tradeName,
        companyName = companyName,
        street = street,
        number = number,
        complement = complement,
        neighborhood = neighborhood,
        city = city,
        state = state,
        zipCode = zipCode,
        phone = phone,
        email = email,
        active = active,
        createdAt = createdDate?.toString() ?: "",
        updatedAt = lastModifiedDate?.toString(),
    )

fun Tenant.toListOption(): TenantListOption =
    TenantListOption(
        id = requireNotNull(id) { "Tenant id must not be null" }.toString(),
        name = tradeName,
    )

fun TenantIdName.toListOption(): TenantListOption =
    TenantListOption(
        id = id.toString(),
        name = name,
    )
