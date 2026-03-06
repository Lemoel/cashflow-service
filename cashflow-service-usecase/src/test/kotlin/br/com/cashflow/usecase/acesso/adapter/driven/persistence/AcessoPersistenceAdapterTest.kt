package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.entity.PerfilUsuario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class AcessoPersistenceAdapterTest {
    private val acessoRepository: AcessoRepository = mockk()
    private lateinit var adapter: AcessoPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = AcessoPersistenceAdapter(acessoRepository)
    }

    @Test
    fun `findByEmail delegates to repository findById and returns entity when found`() {
        val email = "user@test.com"
        val acesso = Acesso(email = email, password = "hash", ativo = true, tipoAcesso = PerfilUsuario.ADMIN.name)
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
        val acesso = Acesso(email = email, password = "old-hash", ativo = true, tipoAcesso = PerfilUsuario.USER.name)
        every { acessoRepository.findById(email) } returns Optional.of(acesso)
        every { acessoRepository.save(match { it.password == "new-hash" }) } returns acesso

        adapter.updatePassword(email, "new-hash")

        verify(exactly = 1) { acessoRepository.findById(email) }
        verify(exactly = 1) { acessoRepository.save(match { it.email == email && it.password == "new-hash" }) }
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
}
