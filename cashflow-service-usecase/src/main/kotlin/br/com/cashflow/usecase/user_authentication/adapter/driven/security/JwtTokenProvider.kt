package br.com.cashflow.usecase.user_authentication.adapter.driven.security

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.user_authentication.model.TokenClaimsModel
import br.com.cashflow.usecase.user_authentication.port.TokenProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @param:Value("\${app.jwt.secret:default-secret-key-min-256-bits-for-hs256-algorithm}")
    private val secret: String,
    @param:Value("\${app.jwt.expiration-ms:900000}")
    private val expirationMs: Long,
    @param:Value("\${app.jwt.refresh-expiration-ms:86400000}")
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
    ): String = buildToken(acesso, tenantId, expirationMs, refresh = false)

    override fun generateRefreshToken(
        acesso: Acesso,
        tenantId: UUID?,
    ): String = buildToken(acesso, tenantId, refreshExpirationMs, refresh = true)

    override fun validateToken(token: String): TokenClaimsModel? =
        parsePayload(token)?.let {
            payloadToClaimsModel(it)
        }

    override fun validateRefreshToken(token: String): TokenClaimsModel? =
        parsePayload(token)?.let { payload ->
            val refreshClaim = payload.get(CLAIM_REFRESH)
            if (refreshClaim != true && refreshClaim != java.lang.Boolean.TRUE) return null
            payloadToClaimsModel(payload)
        }

    private fun buildToken(
        acesso: Acesso,
        tenantId: UUID?,
        expirationMs: Long,
        refresh: Boolean,
    ): String {
        val now = Date()
        var builder =
            Jwts
                .builder()
                .subject(acesso.email)
                .claim(CLAIM_EMAIL, acesso.email)
                .claim(CLAIM_PERFIL, acesso.perfil().name)
                .claim(CLAIM_TENANT_ID, tenantId?.toString())
                .issuedAt(now)
                .expiration(Date(now.time + expirationMs))
        if (refresh) builder = builder.claim(CLAIM_REFRESH, true)
        return builder.signWith(secretKey).compact()
    }

    private fun parsePayload(token: String): Claims? =
        try {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: Exception) {
            null
        }

    private fun payloadToClaimsModel(payload: Claims): TokenClaimsModel? {
        val sub = payload.subject ?: return null
        val email = payload.get(CLAIM_EMAIL, String::class.java) ?: sub
        val perfil = payload.get(CLAIM_PERFIL, String::class.java) ?: return null
        val tenantIdStr = payload.get(CLAIM_TENANT_ID, String::class.java)
        val tenantId = tenantIdStr?.let { UUID.fromString(it) }
        return TokenClaimsModel(sub = sub, email = email, perfil = perfil, tenantId = tenantId)
    }

    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_PERFIL = "perfil"
        private const val CLAIM_TENANT_ID = "tenantId"
        private const val CLAIM_REFRESH = "refresh"
    }
}
