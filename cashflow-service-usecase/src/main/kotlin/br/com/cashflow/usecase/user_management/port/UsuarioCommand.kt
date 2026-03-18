package br.com.cashflow.usecase.user_management.port

import java.util.UUID

data class UsuarioCommand(
    val nome: String,
    val email: String,
    val telefone: String?,
    val perfil: String,
    val congregacaoId: UUID,
    val ativo: Boolean,
) {
    fun sanitized(): UsuarioCommand =
        UsuarioCommand(
            nome = nome.trim().uppercase(),
            email = email.trim().lowercase(),
            telefone = telefone?.trim()?.takeIf { it.isNotBlank() },
            perfil = perfil.trim().uppercase(),
            congregacaoId = congregacaoId,
            ativo = ativo,
        )
}
