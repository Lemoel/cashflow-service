package br.com.cashflow.usecase.user_authentication.adapter.driven.security

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.user_authentication.model.TokenClaims
import br.com.cashflow.usecase.user_authentication.port.TokenProvider
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret:default-secret-key-min-256-bits-for-hs256-algorithm}")
    private val secret: String,
    @Value("\${app.jwt.expiration-ms:3600000}")
    private val expirationMs: Long,
    @Value("\${app.jwt.refresh-expiration-ms:604800000}")
    private val refreshExpirationMs: Long,
) : TokenProvider {
    private val secretKey: SecretKey by lazy {
        val bytes = secret.encodeToByteArray()
        val keyBytes = if (bytes.size < 32) bytes.copyOf(32) else bytes
        Keys.hmacShaKeyFor(keyBytes)
    }

    override fun generateToken(
        acesso: Acesso,
        tenantId: UUID?,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(acesso.email)
            .claim(CLAIM_EMAIL, acesso.email)
            .claim(CLAIM_PERFIL, acesso.perfil().name)
            .claim(CLAIM_TENANT_ID, tenantId?.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    override fun generateRefreshToken(
        acesso: Acesso,
        tenantId: UUID?,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(acesso.email)
            .claim(CLAIM_EMAIL, acesso.email)
            .claim(CLAIM_PERFIL, acesso.perfil().name)
            .claim(CLAIM_TENANT_ID, tenantId?.toString())
            .claim(CLAIM_REFRESH, true)
            .issuedAt(now)
            .expiration(Date(now.time + refreshExpirationMs))
            .signWith(secretKey)
            .compact()
    }

    override fun validateToken(token: String): TokenClaims? {
        return try {
            val payload =
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            val tenantIdStr = payload.get(CLAIM_TENANT_ID, String::class.java)
            val tenantId = tenantIdStr?.let { UUID.fromString(it) }
            TokenClaims(
                sub = payload.subject ?: return null,
                email = payload.get(CLAIM_EMAIL, String::class.java) ?: payload.subject ?: return null,
                perfil = payload.get(CLAIM_PERFIL, String::class.java) ?: return null,
                tenantId = tenantId,
            )
        } catch (error: Exception) {
            null
        }
    }

    override fun validateRefreshToken(token: String): TokenClaims? {
        return try {
            val payload =
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            val refreshClaim = payload.get(CLAIM_REFRESH)
            if (refreshClaim != true && refreshClaim != java.lang.Boolean.TRUE) return null
            val tenantIdStr = payload.get(CLAIM_TENANT_ID, String::class.java)
            val tenantId = tenantIdStr?.let { UUID.fromString(it) }
            TokenClaims(
                sub = payload.subject ?: return null,
                email = payload.get(CLAIM_EMAIL, String::class.java) ?: payload.subject ?: return null,
                perfil = payload.get(CLAIM_PERFIL, String::class.java) ?: return null,
                tenantId = tenantId,
            )
        } catch (error: Exception) {
            null
        }
    }

    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_PERFIL = "perfil"
        private const val CLAIM_TENANT_ID = "tenantId"
        private const val CLAIM_REFRESH = "refresh"
    }
}
