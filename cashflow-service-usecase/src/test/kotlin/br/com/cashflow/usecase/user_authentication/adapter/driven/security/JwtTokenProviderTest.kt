package br.com.cashflow.usecase.user_authentication.adapter.driven.security

import br.com.cashflow.usecase.acesso.entity.Acesso
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtTokenProviderTest {
    private val secret = "default-secret-key-min-256-bits-for-hs256-algorithm"
    private val expirationMs = 3600000L
    private val refreshExpirationMs = 86400000L
    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider(secret, expirationMs, refreshExpirationMs)
    }

    @Test
    fun `generateToken returns valid token that validateToken parses`() {
        val acesso = Acesso(email = "user@test.com", tipoAcesso = "ADMIN")
        val tenantId = UUID.randomUUID()

        val token = provider.generateToken(acesso, tenantId)

        assertThat(token).isNotBlank()
        val claims = provider.validateToken(token)
        assertThat(claims).isNotNull
        assertThat(claims!!.sub).isEqualTo("user@test.com")
        assertThat(claims.email).isEqualTo("user@test.com")
        assertThat(claims.perfil).isEqualTo("ADMIN")
        assertThat(claims.tenantId).isEqualTo(tenantId)
    }

    @Test
    fun `generateToken with null tenantId yields claims with null tenantId`() {
        val acesso = Acesso(email = "u@x.com", tipoAcesso = "USER")
        val token = provider.generateToken(acesso, null)
        val claims = provider.validateToken(token)
        assertThat(claims).isNotNull
        assertThat(claims!!.tenantId).isNull()
    }

    @Test
    fun `generateRefreshToken returns token that validateRefreshToken parses`() {
        val acesso = Acesso(email = "refresh@test.com", tipoAcesso = "USER")
        val tenantId = UUID.randomUUID()

        val token = provider.generateRefreshToken(acesso, tenantId)

        assertThat(token).isNotBlank()
        val claims = provider.validateRefreshToken(token)
        assertThat(claims).isNotNull
        assertThat(claims!!.sub).isEqualTo("refresh@test.com")
        assertThat(claims.email).isEqualTo("refresh@test.com")
        assertThat(claims.perfil).isEqualTo("USER")
    }

    @Test
    fun `validateToken returns null for invalid token`() {
        val result = provider.validateToken("invalid.jwt.token")
        assertThat(result).isNull()
    }

    @Test
    fun `validateToken returns null for empty string`() {
        val result = provider.validateToken("")
        assertThat(result).isNull()
    }

    @Test
    fun `validateRefreshToken returns null when token is access token`() {
        val acesso = Acesso(email = "a@b.com", tipoAcesso = "ADMIN")
        val accessToken = provider.generateToken(acesso, null)
        val claims = provider.validateRefreshToken(accessToken)
        assertThat(claims).isNull()
    }

    @Test
    fun `validateRefreshToken returns null for invalid token`() {
        val result = provider.validateRefreshToken("invalid.refresh.token")
        assertThat(result).isNull()
    }

    @Test
    fun `validateToken returns null for tampered token`() {
        val acesso = Acesso(email = "x@y.com", tipoAcesso = "USER")
        val token = provider.generateToken(acesso, null)
        val tampered = token.dropLast(2) + "xx"
        val claims = provider.validateToken(tampered)
        assertThat(claims).isNull()
    }

    @Test
    fun `secret shorter than 32 bytes is padded`() {
        val shortSecretProvider = JwtTokenProvider("short", expirationMs, refreshExpirationMs)
        val acesso = Acesso(email = "pad@test.com", tipoAcesso = "USER")
        val token = shortSecretProvider.generateToken(acesso, null)
        val claims = shortSecretProvider.validateToken(token)
        assertThat(claims).isNotNull
        assertThat(claims!!.email).isEqualTo("pad@test.com")
    }
}
