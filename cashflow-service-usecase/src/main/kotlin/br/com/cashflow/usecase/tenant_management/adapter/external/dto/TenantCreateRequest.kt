package br.com.cashflow.usecase.tenant_management.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class TenantCreateRequest(

    @field:NotBlank(message = "tradeName is required")
    @field:Size(max = 100)
    val tradeName: String,

    @field:Size(max = 150)
    val companyName: String? = null,

    @field:NotBlank(message = "cnpj is required")
    @field:Size(max = 18)
    val cnpj: String,

    @field:NotBlank(message = "street is required")
    @field:Size(max = 100)
    val street: String,

    @field:NotBlank(message = "number is required")
    @field:Size(max = 10)
    val number: String,

    @field:Size(max = 50)
    val complement: String? = null,

    @field:Size(max = 50)
    val neighborhood: String? = null,

    @field:NotBlank(message = "city is required")
    @field:Size(max = 60)
    val city: String,

    @field:NotBlank(message = "state is required")
    @field:Size(min = 2, max = 2)
    val state: String,

    @field:NotBlank(message = "zipCode is required")
    @field:Size(max = 9)
    val zipCode: String,

    @field:Size(max = 20)
    val phone: String? = null,

    @field:Size(max = 100)
    val email: String? = null,
    val active: Boolean = true,
)
