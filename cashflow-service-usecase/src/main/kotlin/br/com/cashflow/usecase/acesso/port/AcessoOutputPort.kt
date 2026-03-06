package br.com.cashflow.usecase.acesso.port

import br.com.cashflow.usecase.acesso.entity.Acesso
import java.util.UUID

interface AcessoOutputPort {
    fun findByEmail(email: String): Acesso?

    fun updatePassword(
        email: String,
        passwordHash: String,
    )

    fun findTenantIdByEmail(email: String): UUID?
}
