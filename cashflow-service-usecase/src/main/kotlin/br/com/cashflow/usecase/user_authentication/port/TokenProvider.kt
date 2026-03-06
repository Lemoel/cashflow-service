package br.com.cashflow.usecase.user_authentication.port

import br.com.cashflow.usecase.acesso.entity.Acesso
import java.util.UUID

data class TokenClaims(
    val sub: String,
    val email: String,
    val perfil: String,
    val tenantId: UUID?,
)

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
