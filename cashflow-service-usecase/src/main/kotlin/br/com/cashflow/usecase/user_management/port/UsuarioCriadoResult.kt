package br.com.cashflow.usecase.user_management.port

import br.com.cashflow.usecase.acesso.model.AcessoListItem

data class UsuarioCriadoResult(
    val usuario: AcessoListItem,
    val senhaTemporaria: String,
)
