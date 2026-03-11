package br.com.cashflow.usecase.user_management.port

import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import java.util.UUID

interface UserManagementInputPort {
    fun create(command: UsuarioCommand): UsuarioCriadoResult

    fun update(
        id: String,
        command: UsuarioCommand,
    ): AcessoListItem

    fun findById(id: String): AcessoListItem?

    fun findAll(
        page: Int,
        size: Int,
        email: String?,
        congregacaoId: UUID?,
        perfil: String?,
        ativo: Boolean?,
    ): AcessoPage

    fun delete(id: String)

    fun isEmailAvailable(
        email: String,
        excludeId: String?,
    ): Boolean
}
