package br.com.cashflow.usecase.user_authentication.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.InactiveUserException
import br.com.cashflow.commons.exception.InvalidCredentialsException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.commons.exception.WrongPasswordException
import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.entity.PerfilUsuario
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.user_authentication.legacy.LegacyPasswordSupport
import br.com.cashflow.usecase.user_authentication.model.TokenClaimsModel
import br.com.cashflow.usecase.user_authentication.port.TokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {
    private val acessoOutputPort: AcessoOutputPort = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val tenantOutputPort: TenantOutputPort = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private lateinit var service: AuthService

    @BeforeEach
    fun setUp() {
        service =
            AuthService(
                acessoOutputPort = acessoOutputPort,
                tokenProvider = tokenProvider,
                tenantOutputPort = tenantOutputPort,
                passwordEncoder = passwordEncoder,
                expirationMs = 3600000L,
            )
    }

    @Test
    fun `login returns LoginResponse when credentials are valid with BCrypt`() {
        val email = "user@test.com"
        val senha = "password123"
        val bcryptHash = "\$2a\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        val acesso =
            Acesso(
                email = email,
                password = bcryptHash,
                nome = "User",
                ativo = true,
                tipoAcesso = PerfilUsuario.ADMIN.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.matches(senha, acesso.password) } returns true
        every { acessoOutputPort.findTenantIdByEmail(email) } returns null
        every { tokenProvider.generateToken(acesso, null) } returns "jwt-token"
        every { tokenProvider.generateRefreshToken(acesso, null) } returns "refresh-token"

        val result = service.login(email, senha)

        assertThat(result.token).isEqualTo("jwt-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.usuario.email).isEqualTo(email)
        assertThat(result.usuario.perfil).isEqualTo("ADMIN")
        verify(exactly = 1) { acessoOutputPort.findByEmail(email) }
        verify(exactly = 0) { acessoOutputPort.updatePassword(any(), any()) }
    }

    @Test
    fun `login throws InvalidCredentialsException when email not found`() {
        every { acessoOutputPort.findByEmail("unknown@test.com") } returns null

        assertThatThrownBy { service.login("unknown@test.com", "password") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }

    @Test
    fun `login returns LoginResponse when credentials are valid with SHA-256 legacy hash`() {
        val email = "legacy@test.com"
        val senha = "senha123"
        val sha256Hash = LegacyPasswordSupport.sha256Hex(senha)!!
        val acesso =
            Acesso(
                email = email,
                password = sha256Hash,
                nome = "Legacy User",
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.encode(senha) } returns
            "\$2a\$12\$migratedBcryptHashPlaceholder1234567890123456789012"
        every { acessoOutputPort.updatePassword(email, any()) } returns Unit
        every { acessoOutputPort.findTenantIdByEmail(email) } returns null
        every { tokenProvider.generateToken(acesso, null) } returns "jwt-token"
        every { tokenProvider.generateRefreshToken(acesso, null) } returns "refresh-token"

        val result = service.login(email, senha)

        assertThat(result.token).isEqualTo("jwt-token")
        assertThat(result.usuario.email).isEqualTo(email)
        verify(exactly = 1) {
            acessoOutputPort.updatePassword(
                email,
                "\$2a\$12\$migratedBcryptHashPlaceholder1234567890123456789012",
            )
        }
    }

    @Test
    fun `login with SHA-256 triggers migration to BCrypt`() {
        val email = "migrate@test.com"
        val senha = "admin"
        val sha256Hash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"
        val acesso =
            Acesso(
                email = email,
                password = sha256Hash,
                nome = "Migrate User",
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        val newBcryptHash = "\$2b\$12\$newBcryptHashAfterMigration123456789012345678901234"
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.encode(senha) } returns newBcryptHash
        every { acessoOutputPort.updatePassword(email, any()) } returns Unit
        every { acessoOutputPort.findTenantIdByEmail(email) } returns null
        every { tokenProvider.generateToken(acesso, null) } returns "jwt"
        every { tokenProvider.generateRefreshToken(acesso, null) } returns "refresh"

        service.login(email, senha)

        verify(exactly = 1) { acessoOutputPort.updatePassword(email, newBcryptHash) }
    }

    @Test
    fun `login throws InvalidCredentialsException when password does not match`() {
        val email = "user@test.com"
        val storedSha256 = LegacyPasswordSupport.sha256Hex("correct")!!
        val acesso =
            Acesso(
                email = email,
                password = storedSha256,
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso

        assertThatThrownBy { service.login(email, "wrong") }
            .isInstanceOf(InvalidCredentialsException::class.java)
        verify(exactly = 0) { acessoOutputPort.updatePassword(any(), any()) }
    }

    @Test
    fun `login throws InactiveUserException when user is inactive`() {
        val email = "user@test.com"
        val bcryptHash = "\$2a\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        val acesso =
            Acesso(
                email = email,
                password = bcryptHash,
                ativo = false,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso

        assertThatThrownBy { service.login(email, "password") }
            .isInstanceOf(InactiveUserException::class.java)
    }

    @Test
    fun `refresh returns LoginResponse when token is valid`() {
        val email = "user@test.com"
        val claims = TokenClaimsModel(sub = email, email = email, perfil = "ADMIN", tenantId = null)
        val acesso =
            Acesso(
                email = email,
                password = "hash",
                ativo = true,
                tipoAcesso = PerfilUsuario.ADMIN.name,
            )
        every { tokenProvider.validateRefreshToken("valid-refresh") } returns claims
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { acessoOutputPort.findTenantIdByEmail(email) } returns null
        every { tokenProvider.generateToken(acesso, null) } returns "new-token"
        every { tokenProvider.generateRefreshToken(acesso, null) } returns "new-refresh"

        val result = service.refresh("valid-refresh")

        assertThat(result.token).isEqualTo("new-token")
        assertThat(result.usuario.email).isEqualTo(email)
    }

    @Test
    fun `refresh throws InvalidCredentialsException when token is invalid`() {
        every { tokenProvider.validateRefreshToken("invalid") } returns null

        assertThatThrownBy { service.refresh("invalid") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }

    @Test
    fun `refresh throws InactiveUserException when user is inactive`() {
        val email = "user@test.com"
        val claims = TokenClaimsModel(sub = email, email = email, perfil = "ADMIN", tenantId = null)
        val acesso =
            Acesso(
                email = email,
                password = "hash",
                ativo = false,
                tipoAcesso = PerfilUsuario.ADMIN.name,
            )
        every { tokenProvider.validateRefreshToken("valid-refresh") } returns claims
        every { acessoOutputPort.findByEmail(email) } returns acesso

        assertThatThrownBy { service.refresh("valid-refresh") }
            .isInstanceOf(InactiveUserException::class.java)
    }

    @Test
    fun `getCurrentUser returns UsuarioResponse when user exists`() {
        val email = "user@test.com"
        val acesso =
            Acesso(
                email = email,
                nome = "User",
                ativo = true,
                tipoAcesso = PerfilUsuario.FISCAL.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { acessoOutputPort.findTenantIdByEmail(email) } returns null

        val result = service.getCurrentUser(email)

        assertThat(result.id).isEqualTo(email)
        assertThat(result.email).isEqualTo(email)
        assertThat(result.perfil).isEqualTo("FISCAL")
    }

    @Test
    fun `getCurrentUser throws ResourceNotFoundException when email does not exist`() {
        every { acessoOutputPort.findByEmail("unknown@test.com") } returns null

        assertThatThrownBy { service.getCurrentUser("unknown@test.com") }
            .isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `changePassword updates password when current password is correct with BCrypt`() {
        val email = "user@test.com"
        val bcryptHash = "\$2a\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        val acesso =
            Acesso(
                email = email,
                password = bcryptHash,
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.matches("current", acesso.password) } returns true
        every { passwordEncoder.encode("newpass123") } returns "new-hash"
        every { acessoOutputPort.updatePassword(email, "new-hash") } returns Unit

        service.changePassword(email, "current", "newpass123")

        verify(exactly = 1) { acessoOutputPort.updatePassword(email, "new-hash") }
    }

    @Test
    fun `changePassword updates password when current password is stored as SHA-256`() {
        val email = "user@test.com"
        val currentSha256 = LegacyPasswordSupport.sha256Hex("current")!!
        val acesso =
            Acesso(
                email = email,
                password = currentSha256,
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.encode("newpass123") } returns "new-bcrypt-hash"
        every { acessoOutputPort.updatePassword(email, "new-bcrypt-hash") } returns Unit

        service.changePassword(email, "current", "newpass123")

        verify(exactly = 1) { acessoOutputPort.updatePassword(email, "new-bcrypt-hash") }
    }

    @Test
    fun `changePassword throws WrongPasswordException when current password is wrong`() {
        val email = "user@test.com"
        val storedSha256 = LegacyPasswordSupport.sha256Hex("correct")!!
        val acesso =
            Acesso(
                email = email,
                password = storedSha256,
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso

        assertThatThrownBy { service.changePassword(email, "wrong", "newpass123") }
            .isInstanceOf(WrongPasswordException::class.java)
    }

    @Test
    fun `changePassword throws ResourceNotFoundException when email not found`() {
        every { acessoOutputPort.findByEmail("unknown@test.com") } returns null

        assertThatThrownBy { service.changePassword("unknown@test.com", "current", "newpass123") }
            .isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `changePassword throws BusinessException when new password is too short`() {
        val email = "user@test.com"
        val bcryptHash = "\$2a\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        val acesso =
            Acesso(
                email = email,
                password = bcryptHash,
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoOutputPort.findByEmail(email) } returns acesso
        every { passwordEncoder.matches("current", any()) } returns true

        assertThatThrownBy { service.changePassword(email, "current", "short") }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(AuthService.MSG_NEW_PASSWORD_MIN_LENGTH)
    }
}
