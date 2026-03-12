package br.com.cashflow.usecase.user_management.port

import java.util.UUID

data class UsuarioCommand(
    val nome: String,
    val email: String,
    val telefone: String?,
    val perfil: String,
    val congregacaoId: UUID,
    val ativo: Boolean,
)
