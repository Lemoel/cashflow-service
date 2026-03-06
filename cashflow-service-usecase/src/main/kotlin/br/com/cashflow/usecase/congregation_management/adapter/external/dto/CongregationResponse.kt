package br.com.cashflow.usecase.congregation_management.adapter.external.dto

import br.com.cashflow.commons.util.CnpjValidator
import br.com.cashflow.usecase.congregation.entity.Congregation

fun CongregationCreateRequest.toEntity(): Congregation {
    val cnpjDigits = cnpj?.let { CnpjValidator.clean(it).takeIf { d -> d.isNotBlank() } }
    return Congregation(
        tenantId = tenantId,
        setorialId = setorialId,
        nome = nome.trim().uppercase(),
        cnpj = cnpjDigits,
        logradouro = logradouro.trim(),
        bairro = bairro.trim().uppercase(),
        numero = numero.trim(),
        cidade = cidade.trim().uppercase(),
        uf = uf.trim().uppercase(),
        cep = cep.trim(),
        email = email?.trim()?.lowercase(),
        telefone = telefone?.trim(),
        ativo = ativo,
    )
}

fun CongregationUpdateRequest.applyTo(congregation: Congregation) {
    congregation.setorialId = setorialId
    congregation.nome = nome.trim().uppercase()
    congregation.cnpj = cnpj?.let { CnpjValidator.clean(it).takeIf { d -> d.isNotBlank() } }
    congregation.logradouro = logradouro.trim()
    congregation.bairro = bairro.trim().uppercase()
    congregation.numero = numero.trim()
    congregation.cidade = cidade.trim().uppercase()
    congregation.uf = uf.trim().uppercase()
    congregation.cep = cep.trim()
    congregation.email = email?.trim()?.lowercase()
    congregation.telefone = telefone?.trim()
    congregation.ativo = ativo
}

data class CongregationResponse(
    val id: String,
    val tenantId: String,
    val setorialId: String?,
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
    val ativo: Boolean,
    val createdAt: String,
    val updatedAt: String?,
)

data class CongregationListOption(
    val id: String,
    val nome: String,
)

data class CongregationListResponse(
    val items: List<CongregationResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class CnpjUnicoCongregationResponse(
    val unique: Boolean,
)

fun Congregation.toResponse(): CongregationResponse =
    CongregationResponse(
        id = id!!.toString(),
        tenantId = tenantId!!.toString(),
        setorialId = setorialId?.toString(),
        nome = nome,
        cnpj = cnpj,
        logradouro = logradouro,
        bairro = bairro,
        numero = numero,
        cidade = cidade,
        uf = uf,
        cep = cep,
        email = email,
        telefone = telefone,
        ativo = ativo,
        createdAt = createdAt?.toString() ?: "",
        updatedAt = updatedAt?.toString(),
    )

fun Congregation.toListOption(): CongregationListOption =
    CongregationListOption(
        id = id!!.toString(),
        nome = nome,
    )
