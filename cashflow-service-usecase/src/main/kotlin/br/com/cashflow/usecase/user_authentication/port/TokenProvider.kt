package br.com.cashflow.usecase.user_authentication.port

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.user_authentication.model.TokenClaims
import java.util.UUID

interface TokenProvider {
    fun generateToken(
        acesso: Acesso,
        tenantId: java.util.UUID?,
    ): String

    fun generateRefreshToken(
        acesso: Acesso,
        tenantId: java.util.UUID?,
    ): String

    fun validateToken(token: String): TokenClaims?

    fun validateRefreshToken(token: String): TokenClaims?
}
