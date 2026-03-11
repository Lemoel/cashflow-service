package br.com.cashflow.usecase.user_management.adapter.external.dto

import br.com.cashflow.usecase.acesso.model.AcessoListItem

data class UsuarioResponseDto(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String?,
    val perfil: String,
    val congregacaoId: String?,
    val congregacaoNome: String?,
    val ativo: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
)

data class UsuarioListResponseDto(
    val items: List<UsuarioResponseDto>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class EmailUnicoResponseDto(
    val unico: Boolean,
)

fun AcessoListItem.toUsuarioResponseDto(): UsuarioResponseDto =
    UsuarioResponseDto(
        id = email,
        nome = nome ?: "",
        email = email,
        telefone = telefone,
        perfil = tipoAcesso,
        congregacaoId = congregacaoId?.toString(),
        congregacaoNome = congregacaoNome,
        ativo = ativo,
        createdAt = data?.toString(),
        updatedAt = modDateTime?.toString(),
    )
