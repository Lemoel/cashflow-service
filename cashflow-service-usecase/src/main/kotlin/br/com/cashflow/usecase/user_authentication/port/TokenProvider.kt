package br.com.cashflow.usecase.user_authentication.port

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.user_authentication.model.TokenClaimsModel

interface TokenProvider {
    fun generateToken(
        acesso: Acesso,
        tenantId: java.util.UUID?,
    ): String

    fun generateRefreshToken(
        acesso: Acesso,
        tenantId: java.util.UUID?,
    ): String

    fun validateToken(token: String): TokenClaimsModel?

    fun validateRefreshToken(token: String): TokenClaimsModel?
}
