package br.com.cashflow.usecase.acesso.port

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import java.util.UUID

interface AcessoOutputPort {
    fun findByEmail(email: String): Acesso?

    fun existsByEmail(email: String): Boolean

    fun updatePassword(
        email: String,
        passwordHash: String,
    )

    fun findTenantIdByEmail(email: String): UUID?

    fun save(acesso: Acesso): Acesso

    fun existsByEmailExcluding(
        emailToCheck: String,
        excludeEmail: String?,
    ): Boolean

    fun findAll(
        filter: AcessoFilter?,
        page: Int,
        size: Int,
    ): AcessoPage

    fun deleteByEmail(email: String)

    fun setCongregacaoForEmail(
        email: String,
        congregacaoId: UUID,
    )

    fun findListItemByEmail(email: String): AcessoListItem?

    fun insertUserTenantMap(
        email: String,
        tenantId: UUID,
    )
}
