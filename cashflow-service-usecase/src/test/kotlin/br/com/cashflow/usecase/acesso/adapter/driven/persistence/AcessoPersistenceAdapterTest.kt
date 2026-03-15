package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.entity.PerfilUsuario
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.util.Optional
import java.util.UUID

class AcessoPersistenceAdapterTest {
    private val acessoRepository: AcessoRepository = mockk()
    private val jdbcTemplate: JdbcTemplate = mockk(relaxed = true)
    private lateinit var adapter: AcessoPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = AcessoPersistenceAdapter(acessoRepository, jdbcTemplate)
    }

    @Test
    fun `findByEmail delegates to repository findById and returns entity when found`() {
        val email = "user@test.com"
        val acesso =
            Acesso(
                email = email,
                password = "hash",
                ativo = true,
                tipoAcesso = PerfilUsuario.ADMIN.name,
            )
        every { acessoRepository.findById(email) } returns Optional.of(acesso)

        val result = adapter.findByEmail(email)

        assertThat(result).isEqualTo(acesso)
        verify(exactly = 1) { acessoRepository.findById(email) }
    }

    @Test
    fun `findByEmail returns null when not found`() {
        every { acessoRepository.findById("unknown@test.com") } returns Optional.empty()

        val result = adapter.findByEmail("unknown@test.com")

        assertThat(result).isNull()
        verify(exactly = 1) { acessoRepository.findById("unknown@test.com") }
    }

    @Test
    fun `updatePassword loads entity sets password and saves`() {
        val email = "user@test.com"
        val acesso =
            Acesso(
                email = email,
                password = "old-hash",
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoRepository.findById(email) } returns Optional.of(acesso)
        every { acessoRepository.save(match { it.password == "new-hash" }) } returns acesso

        adapter.updatePassword(email, "new-hash")

        verify(exactly = 1) { acessoRepository.findById(email) }
        verify(exactly = 1) {
            acessoRepository.save(
                match {
                    it.email == email &&
                        it.password == "new-hash"
                },
            )
        }
    }

    @Test
    fun `findTenantIdByEmail delegates to repository and returns tenant id`() {
        val email = "user@test.com"
        val tenantId = UUID.randomUUID()
        every { acessoRepository.findTenantIdByEmail(email) } returns tenantId

        val result = adapter.findTenantIdByEmail(email)

        assertThat(result).isEqualTo(tenantId)
        verify(exactly = 1) { acessoRepository.findTenantIdByEmail(email) }
    }

    @Test
    fun `findTenantIdByEmail returns null when no congregation link`() {
        every { acessoRepository.findTenantIdByEmail("user@test.com") } returns null

        val result = adapter.findTenantIdByEmail("user@test.com")

        assertThat(result).isNull()
    }

    @Test
    fun `updatePassword does nothing when user not found`() {
        val email = "notfound@test.com"
        every { acessoRepository.findById(email) } returns Optional.empty()

        adapter.updatePassword(email, "new-hash")

        verify(exactly = 1) { acessoRepository.findById(email) }
        verify(exactly = 0) { acessoRepository.save(any<Acesso>()) }
    }

    @Test
    fun `save inserts via jdbcTemplate when entity is new`() {
        val acesso =
            Acesso(
                email = "new@test.com",
                password = "hash",
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoRepository.existsById("new@test.com") } returns false
        every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1
        every { acessoRepository.findById("new@test.com") } returns Optional.of(acesso)

        val result = adapter.save(acesso)

        assertThat(result).isEqualTo(acesso)
        verify(exactly = 1) { jdbcTemplate.update(any<String>(), *anyVararg()) }
        verify(exactly = 0) { acessoRepository.save(any<Acesso>()) }
    }

    @Test
    fun `save delegates to repository save when entity already exists`() {
        val acesso =
            Acesso(
                email = "existing@test.com",
                password = "hash",
                ativo = true,
                tipoAcesso = PerfilUsuario.USER.name,
            )
        every { acessoRepository.existsById("existing@test.com") } returns true
        every { acessoRepository.save(acesso) } returns acesso

        val result = adapter.save(acesso)

        assertThat(result).isEqualTo(acesso)
        verify(exactly = 1) { acessoRepository.save(acesso) }
        verify(exactly = 0) { jdbcTemplate.update(any<String>(), *anyVararg()) }
    }

    @Test
    fun `existsByEmailExcluding returns false when email does not exist`() {
        every { acessoRepository.existsById("new@test.com") } returns false

        val result = adapter.existsByEmailExcluding("new@test.com", null)

        assertThat(result).isFalse()
    }

    @Test
    fun `existsByEmailExcluding returns true when email exists and no exclude`() {
        every { acessoRepository.existsById("user@test.com") } returns true

        val result = adapter.existsByEmailExcluding("user@test.com", null)

        assertThat(result).isTrue()
    }

    @Test
    fun `existsByEmailExcluding returns false when email equals excludeEmail`() {
        every { acessoRepository.existsById("user@test.com") } returns true

        val result = adapter.existsByEmailExcluding("user@test.com", "user@test.com")

        assertThat(result).isFalse()
    }

    @Test
    fun `existsByEmailExcluding returns true when email exists and differs from excludeEmail`() {
        every { acessoRepository.existsById("user@test.com") } returns true

        val result = adapter.existsByEmailExcluding("user@test.com", "other@test.com")

        assertThat(result).isTrue()
    }

    @Test
    fun `findAll delegates to repository and returns AcessoPage`() {
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = "USER",
                telefone = null,
                tipoAcesso = "ADMIN",
                ativo = true,
                data = Instant.now(),
                modDateTime = null,
                congregacaoId = UUID.randomUUID(),
                congregacaoNome = "Cong A",
            )
        every { acessoRepository.countFiltered(any()) } returns 1L
        every { acessoRepository.findFiltered(any(), any()) } returns listOf(item)

        val result = adapter.findAll(null, 0, 10)

        assertThat(result).isInstanceOf(AcessoPage::class.java)
        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
    }

    @Test
    fun `findAll with filter delegates correctly`() {
        val filter = AcessoFilter(email = "test")
        every { acessoRepository.countFiltered(filter) } returns 0L
        every { acessoRepository.findFiltered(filter, any()) } returns emptyList()

        val result = adapter.findAll(filter, 1, 20)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
        assertThat(result.page).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(20)
    }

    @Test
    fun `deleteByEmail delegates to repository deleteById`() {
        every { acessoRepository.deleteById("user@test.com") } returns Unit

        adapter.deleteByEmail("user@test.com")

        verify(exactly = 1) { acessoRepository.deleteById("user@test.com") }
    }

    @Test
    fun `setCongregacaoForEmail deletes old and inserts new congregation`() {
        val email = "user@test.com"
        val congId = UUID.randomUUID()

        adapter.setCongregacaoForEmail(email, congId)

        verify(exactly = 1) {
            jdbcTemplate.update(
                "DELETE FROM acesso_congregacao WHERE email = ?",
                email,
            )
        }
        verify(exactly = 1) {
            jdbcTemplate.update(
                "INSERT INTO acesso_congregacao (email, congregacao_id) VALUES (?, ?)",
                email,
                congId,
            )
        }
    }

    @Test
    fun `findListItemByEmail delegates to repository`() {
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = "USER",
                telefone = null,
                tipoAcesso = "ADMIN",
                ativo = true,
                data = null,
                modDateTime = null,
                congregacaoId = null,
                congregacaoNome = null,
            )
        every { acessoRepository.findListItemByEmail("user@test.com") } returns item

        val result = adapter.findListItemByEmail("user@test.com")

        assertThat(result).isEqualTo(item)
        verify(exactly = 1) { acessoRepository.findListItemByEmail("user@test.com") }
    }

    @Test
    fun `findListItemByEmail returns null when not found`() {
        every { acessoRepository.findListItemByEmail("missing@test.com") } returns null

        val result = adapter.findListItemByEmail("missing@test.com")

        assertThat(result).isNull()
    }
}
