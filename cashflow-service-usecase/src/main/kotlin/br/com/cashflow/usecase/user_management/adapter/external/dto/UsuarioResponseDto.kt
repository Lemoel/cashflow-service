package br.com.cashflow.usecase.user_management.adapter.external.dto

import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.user_management.port.UsuarioCriadoResult

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

data class UsuarioCriadoResponseDto(
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
    val senhaTemporaria: String,
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
        createdAt = createdDate?.toString(),
        updatedAt = lastModifiedDate?.toString(),
    )

fun UsuarioCriadoResult.toUsuarioCriadoResponseDto(): UsuarioCriadoResponseDto =
    UsuarioCriadoResponseDto(
        id = usuario.email,
        nome = usuario.nome ?: "",
        email = usuario.email,
        telefone = usuario.telefone,
        perfil = usuario.tipoAcesso,
        congregacaoId = usuario.congregacaoId?.toString(),
        congregacaoNome = usuario.congregacaoNome,
        ativo = usuario.ativo,
        createdAt = usuario.createdDate?.toString(),
        updatedAt = usuario.lastModifiedDate?.toString(),
        senhaTemporaria = senhaTemporaria,
    )
