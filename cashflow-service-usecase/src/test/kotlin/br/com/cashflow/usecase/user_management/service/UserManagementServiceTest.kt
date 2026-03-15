package br.com.cashflow.usecase.user_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.user_management.port.UsuarioCommand
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.UUID

class UserManagementServiceTest {
    private val acessoOutputPort: AcessoOutputPort = mockk()
    private val congregationOutputPort: CongregationOutputPort = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private lateinit var service: UserManagementService

    private val congregacaoId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        service =
            UserManagementService(
                acessoOutputPort = acessoOutputPort,
                congregationOutputPort = congregationOutputPort,
                passwordEncoder = passwordEncoder,
            )
    }

    private fun buildListItem(
        email: String = "user@test.com",
        nome: String = "USUARIO TESTE",
        perfil: String = "ADMIN",
    ): AcessoListItem =
        AcessoListItem(
            email = email,
            nome = nome,
            telefone = "11999990000",
            tipoAcesso = perfil,
            ativo = true,
            data = Instant.now(),
            modDateTime = null,
            congregacaoId = congregacaoId,
            congregacaoNome = "SEDE",
        )

    private fun buildCommand(
        email: String = "User@Test.com",
        perfil: String = "ADMIN",
        nome: String = "  Usuario Teste  ",
    ): UsuarioCommand =
        UsuarioCommand(
            nome = nome,
            email = email,
            telefone = "11999990000",
            perfil = perfil,
            congregacaoId = congregacaoId,
            ativo = true,
        )

    private fun buildUpdateCommand(
        email: String = "user@test.com",
        perfil: String = "ADMIN",
    ): UsuarioCommand =
        UsuarioCommand(
            nome = "  Usuario Atualizado  ",
            email = email,
            telefone = "11999991111",
            perfil = perfil,
            congregacaoId = congregacaoId,
            ativo = true,
        )

    private fun buildAcesso(email: String = "user@test.com"): Acesso =
        Acesso(
            email = email,
            password = "\$2a\$12\$hashedPassword",
            data = Instant.now(),
            nome = "USUARIO TESTE",
            ativo = true,
            tipoAcesso = "ADMIN",
        )

    private fun stubCongregationExists() {
        every { congregationOutputPort.findById(congregacaoId) } returns
            Congregation(id = congregacaoId, tenantId = congregacaoId, nome = "SEDE")
    }

    @Test
    fun `create returns UsuarioCriadoResult when email is unique`() {
        val command = buildCommand()
        val expected = buildListItem()

        every { acessoOutputPort.existsByEmailExcluding("user@test.com", null) } returns false
        stubCongregationExists()
        every { passwordEncoder.encode(any()) } returns "\$2a\$12\$temporaryHash"
        every { acessoOutputPort.save(any()) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail("user@test.com", congregacaoId) } just runs
        every { acessoOutputPort.insertUserTenantMap("user@test.com", congregacaoId) } just runs
        every { acessoOutputPort.findListItemByEmail("user@test.com") } returns expected

        val result = service.create(command)

        assertThat(result.usuario).isEqualTo(expected)
        assertThat(result.senhaTemporaria).isNotBlank()
        verify(exactly = 1) { passwordEncoder.encode(any()) }
        verify(exactly = 1) { acessoOutputPort.save(any()) }
        verify(exactly = 1) { acessoOutputPort.setCongregacaoForEmail("user@test.com", congregacaoId) }
    }

    @Test
    fun `create stores nome in uppercase and email in lowercase`() {
        val command = buildCommand(email = "  User@Test.COM  ")
        val acessoSlot = slot<Acesso>()

        every { acessoOutputPort.existsByEmailExcluding("user@test.com", null) } returns false
        stubCongregationExists()
        every { passwordEncoder.encode(any()) } returns "\$2a\$12\$hash"
        every { acessoOutputPort.save(capture(acessoSlot)) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail(any(), any()) } just runs
        every { acessoOutputPort.insertUserTenantMap(any(), any()) } just runs
        every { acessoOutputPort.findListItemByEmail(any()) } returns buildListItem()

        service.create(command)

        assertThat(acessoSlot.captured.nome).isEqualTo("USUARIO TESTE")
        assertThat(acessoSlot.captured.email).isEqualTo("user@test.com")
    }

    @Test
    fun `create throws ConflictException when email already exists`() {
        val command = buildCommand()

        every { acessoOutputPort.existsByEmailExcluding("user@test.com", null) } returns true

        assertThatThrownBy { service.create(command) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um usuário com este e-mail.")
        verify(exactly = 0) { acessoOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when congregacao not found`() {
        val command = buildCommand()

        every { acessoOutputPort.existsByEmailExcluding("user@test.com", null) } returns false
        every { congregationOutputPort.findById(congregacaoId) } returns null

        assertThatThrownBy { service.create(command) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Congregação não encontrada.")
    }

    @Test
    fun `create throws BusinessException when perfil is invalid`() {
        val command = buildCommand(perfil = "INVALIDO")

        every { acessoOutputPort.existsByEmailExcluding("user@test.com", null) } returns false
        stubCongregationExists()

        assertThatThrownBy { service.create(command) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Perfil inválido.")
    }

    @Test
    fun `create generates temporary password hashed with BCrypt`() {
        val command = buildCommand()
        val acessoSlot = slot<Acesso>()

        every { acessoOutputPort.existsByEmailExcluding(any(), any()) } returns false
        stubCongregationExists()
        every { passwordEncoder.encode(any()) } returns "\$2a\$12\$bcryptHashResult"
        every { acessoOutputPort.save(capture(acessoSlot)) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail(any(), any()) } just runs
        every { acessoOutputPort.insertUserTenantMap(any(), any()) } just runs
        every { acessoOutputPort.findListItemByEmail(any()) } returns buildListItem()

        val result = service.create(command)

        assertThat(acessoSlot.captured.password).isEqualTo("\$2a\$12\$bcryptHashResult")
        assertThat(result.senhaTemporaria).hasSize(12)
        verify(exactly = 1) { passwordEncoder.encode(match { it.length == 12 }) }
    }

    @Test
    fun `update returns updated usuario when same email`() {
        val email = "user@test.com"
        val command = buildUpdateCommand(email = email)
        val existing = buildAcesso(email)
        val expected = buildListItem(email = email, nome = "USUARIO ATUALIZADO")

        every { acessoOutputPort.findByEmail(email) } returns existing
        every { acessoOutputPort.existsByEmailExcluding(email, email) } returns false
        stubCongregationExists()
        every { acessoOutputPort.save(any()) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail(email, congregacaoId) } just runs
        every { acessoOutputPort.findListItemByEmail(email) } returns expected

        val result = service.update(email, command)

        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) { acessoOutputPort.save(any()) }
        verify(exactly = 0) { acessoOutputPort.deleteByEmail(any()) }
    }

    @Test
    fun `update with email change deletes old and creates new`() {
        val oldEmail = "old@test.com"
        val newEmail = "new@test.com"
        val command = buildUpdateCommand(email = newEmail)
        val existing = buildAcesso(oldEmail)
        val expected = buildListItem(email = newEmail)

        every { acessoOutputPort.findByEmail(oldEmail) } returns existing
        every { acessoOutputPort.existsByEmailExcluding(newEmail, oldEmail) } returns false
        stubCongregationExists()
        every { acessoOutputPort.save(any()) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail(newEmail, congregacaoId) } just runs
        every { acessoOutputPort.deleteByEmail(oldEmail) } just runs
        every { acessoOutputPort.findListItemByEmail(newEmail) } returns expected

        val result = service.update(oldEmail, command)

        assertThat(result.email).isEqualTo(newEmail)
        verify(exactly = 1) { acessoOutputPort.deleteByEmail(oldEmail) }
        verify(exactly = 1) { acessoOutputPort.save(match { it.email == newEmail }) }
    }

    @Test
    fun `update does not change password`() {
        val email = "user@test.com"
        val command = buildUpdateCommand(email = email)
        val existing = buildAcesso(email)
        val acessoSlot = slot<Acesso>()

        every { acessoOutputPort.findByEmail(email) } returns existing
        every { acessoOutputPort.existsByEmailExcluding(email, email) } returns false
        stubCongregationExists()
        every { acessoOutputPort.save(capture(acessoSlot)) } answers { firstArg() }
        every { acessoOutputPort.setCongregacaoForEmail(any(), any()) } just runs
        every { acessoOutputPort.findListItemByEmail(any()) } returns buildListItem()

        service.update(email, command)

        assertThat(acessoSlot.captured.password).isEqualTo(existing.password)
        verify(exactly = 0) { passwordEncoder.encode(any()) }
    }

    @Test
    fun `update throws ResourceNotFoundException when user not found`() {
        every { acessoOutputPort.findByEmail("unknown@test.com") } returns null

        assertThatThrownBy { service.update("unknown@test.com", buildUpdateCommand()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Usuário não encontrado.")
    }

    @Test
    fun `update throws ConflictException when new email belongs to another user`() {
        val email = "user@test.com"
        val command = buildUpdateCommand(email = "taken@test.com")

        every { acessoOutputPort.findByEmail(email) } returns buildAcesso(email)
        every { acessoOutputPort.existsByEmailExcluding("taken@test.com", email) } returns true

        assertThatThrownBy { service.update(email, command) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um usuário com este e-mail.")
    }

    @Test
    fun `findById returns list item when found`() {
        val expected = buildListItem()
        every { acessoOutputPort.findListItemByEmail("user@test.com") } returns expected

        val result = service.findById("user@test.com")

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `findById returns null when not found`() {
        every { acessoOutputPort.findListItemByEmail("unknown@test.com") } returns null

        val result = service.findById("unknown@test.com")

        assertThat(result).isNull()
    }

    @Test
    fun `findAll delegates to output port with filter`() {
        val page = AcessoPage(emptyList(), 0L, 0, 10)
        every { acessoOutputPort.findAll(any(), 0, 10) } returns page

        val result = service.findAll(0, 10, "admin", null, "ADMIN", true)

        assertThat(result).isEqualTo(page)
        verify(exactly = 1) {
            acessoOutputPort.findAll(
                match<AcessoFilter> {
                    it.email == "admin" && it.perfil == "ADMIN" && it.ativo == true
                },
                0,
                10,
            )
        }
    }

    @Test
    fun `findAll ignores blank filter values`() {
        val page = AcessoPage(emptyList(), 0L, 0, 10)
        every { acessoOutputPort.findAll(any(), 0, 10) } returns page

        service.findAll(0, 10, "  ", null, "", null)

        verify(exactly = 1) {
            acessoOutputPort.findAll(
                match<AcessoFilter> {
                    it.email == null && it.perfil == null && it.ativo == null
                },
                0,
                10,
            )
        }
    }

    @Test
    fun `delete removes user when found`() {
        every { acessoOutputPort.findByEmail("user@test.com") } returns buildAcesso()
        every { acessoOutputPort.deleteByEmail("user@test.com") } just runs

        service.delete("user@test.com")

        verify(exactly = 1) { acessoOutputPort.deleteByEmail("user@test.com") }
    }

    @Test
    fun `delete throws ResourceNotFoundException when user not found`() {
        every { acessoOutputPort.findByEmail("unknown@test.com") } returns null

        assertThatThrownBy { service.delete("unknown@test.com") }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Usuário não encontrado.")
    }

    @Test
    fun `delete throws ConflictException when user has dependent records`() {
        every { acessoOutputPort.findByEmail("user@test.com") } returns buildAcesso()
        every { acessoOutputPort.deleteByEmail("user@test.com") } throws
            DataIntegrityViolationException("FK violation")

        assertThatThrownBy { service.delete("user@test.com") }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("registros vinculados")
    }

    @Test
    fun `isEmailAvailable returns true when email is not taken`() {
        every { acessoOutputPort.existsByEmailExcluding("free@test.com", null) } returns false

        val result = service.isEmailAvailable("free@test.com", null)

        assertThat(result).isTrue()
    }

    @Test
    fun `isEmailAvailable returns false when email is taken by another user`() {
        every { acessoOutputPort.existsByEmailExcluding("taken@test.com", null) } returns true

        val result = service.isEmailAvailable("taken@test.com", null)

        assertThat(result).isFalse()
    }

    @Test
    fun `isEmailAvailable excludes given email from check`() {
        every { acessoOutputPort.existsByEmailExcluding("self@test.com", "self@test.com") } returns false

        val result = service.isEmailAvailable("self@test.com", "self@test.com")

        assertThat(result).isTrue()
        verify(exactly = 1) { acessoOutputPort.existsByEmailExcluding("self@test.com", "self@test.com") }
    }
}
