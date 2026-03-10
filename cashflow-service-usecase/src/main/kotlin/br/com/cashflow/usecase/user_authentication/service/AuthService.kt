package br.com.cashflow.usecase.user_authentication.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.InactiveUserException
import br.com.cashflow.commons.exception.InvalidCredentialsException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.commons.exception.WrongPasswordException
import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.user_authentication.legacy.LegacyPasswordSupport
import br.com.cashflow.usecase.user_authentication.model.LoginResponse
import br.com.cashflow.usecase.user_authentication.model.UsuarioResponse
import br.com.cashflow.usecase.user_authentication.port.AuthInputPort
import br.com.cashflow.usecase.user_authentication.port.TokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val acessoOutputPort: AcessoOutputPort,
    private val tokenProvider: TokenProvider,
    private val tenantOutputPort: TenantOutputPort,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.jwt.expiration-ms:3600000}")
    private val expirationMs: Long,
) : AuthInputPort {
    override fun login(
        email: String,
        password: String,
    ): LoginResponse {
        val acesso =
            acessoOutputPort.findByEmail(email)
                ?: throw InvalidCredentialsException()
        if (!acesso.ativo) {
            throw InactiveUserException()
        }
        if (!passwordMatches(password, acesso.password)) {
            throw InvalidCredentialsException()
        }
        if (!LegacyPasswordSupport.looksLikeBcrypt(acesso.password)) {
            val bcryptHash = requireNotNull(passwordEncoder.encode(password)) { "Password encoding failed" }
            acessoOutputPort.updatePassword(email, bcryptHash)
        }
        val tenantId = acessoOutputPort.findTenantIdByEmail(email)
        val tenantNome = tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        val usuarioResponse = toUsuarioResponse(acesso, tenantId, tenantNome)
        val token = tokenProvider.generateToken(acesso, tenantId)
        val refreshToken = tokenProvider.generateRefreshToken(acesso, tenantId)
        return LoginResponse(
            token = token,
            refreshToken = refreshToken,
            expiresIn = expirationMs / 1000,
            usuario = usuarioResponse,
        )
    }

    override fun refresh(refreshToken: String): LoginResponse {
        val claims =
            tokenProvider.validateRefreshToken(refreshToken)
                ?: throw InvalidCredentialsException()
        val email = claims.sub
        val acesso =
            acessoOutputPort.findByEmail(email)
                ?: throw InvalidCredentialsException()
        if (!acesso.ativo) {
            throw InactiveUserException()
        }
        val tenantId = acessoOutputPort.findTenantIdByEmail(email)
        val tenantNome = tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        val usuarioResponse = toUsuarioResponse(acesso, tenantId, tenantNome)
        val token = tokenProvider.generateToken(acesso, tenantId)
        val newRefreshToken = tokenProvider.generateRefreshToken(acesso, tenantId)
        return LoginResponse(
            token = token,
            refreshToken = newRefreshToken,
            expiresIn = expirationMs / 1000,
            usuario = usuarioResponse,
        )
    }

    override fun getCurrentUser(email: String): UsuarioResponse {
        val acesso =
            acessoOutputPort.findByEmail(email)
                ?: throw ResourceNotFoundException("User not found")
        val tenantId = acessoOutputPort.findTenantIdByEmail(email)
        val tenantNome = tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        return toUsuarioResponse(acesso, tenantId, tenantNome)
    }

    override fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
    ) {
        if (newPassword.length < 6) {
            throw BusinessException(MSG_NEW_PASSWORD_MIN_LENGTH)
        }
        val acesso =
            acessoOutputPort.findByEmail(email)
                ?: throw ResourceNotFoundException("User not found")
        if (!passwordMatches(currentPassword, acesso.password)) {
            throw WrongPasswordException()
        }
        val hash = requireNotNull(passwordEncoder.encode(newPassword)) { "Password encoding failed" }
        acessoOutputPort.updatePassword(email, hash)
    }

    companion object {
        const val MSG_NEW_PASSWORD_MIN_LENGTH = "A nova senha deve ter no mínimo 6 caracteres."
    }

    private fun passwordMatches(
        plainPassword: String,
        storedHash: String,
    ): Boolean =
        if (LegacyPasswordSupport.looksLikeBcrypt(storedHash)) {
            passwordEncoder.matches(plainPassword, storedHash)
        } else {
            LegacyPasswordSupport.matchesSha256Hex(plainPassword, storedHash)
        }

    private fun toUsuarioResponse(
        acesso: Acesso,
        tenantId: UUID?,
        tenantNome: String?,
    ): UsuarioResponse =
        UsuarioResponse(
            id = acesso.email!!,
            nome = acesso.nome,
            email = acesso.email!!,
            perfil = acesso.perfil().name,
            tenantId = tenantId,
            tenantNome = tenantNome,
        )
}
