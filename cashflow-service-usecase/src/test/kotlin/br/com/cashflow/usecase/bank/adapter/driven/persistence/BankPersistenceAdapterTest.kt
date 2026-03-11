package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class BankPersistenceAdapterTest {
    private val bankRepository: BankRepository = mockk()
    private lateinit var adapter: BankPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = BankPersistenceAdapter(bankRepository)
    }

    @Test
    fun `findById delegates to repository and returns bank when found`() {
        val id = UUID.randomUUID()
        val bank =
            Bank(
                id = id,
                nome = "Banco A",
                codigo = "001",
                enderecoCompleto = "",
                tipoIntegracao = "",
                ativo = true,
            )
        every { bankRepository.findById(id) } returns Optional.of(bank)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(bank)
        verify(exactly = 1) { bankRepository.findById(id) }
    }

    @Test
    fun `findById returns null when repository returns empty`() {
        val id = UUID.randomUUID()
        every { bankRepository.findById(id) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { bankRepository.findById(id) }
    }

    @Test
    fun `findByCodigo delegates to repository and returns bank when found`() {
        val id = UUID.randomUUID()
        val bank =
            Bank(
                id = id,
                nome = "PagBank",
                codigo = "290",
                enderecoCompleto = "",
                tipoIntegracao = "API",
                ativo = true,
            )
        every { bankRepository.findByCodigo("290") } returns bank

        val result = adapter.findByCodigo("290")

        assertThat(result).isEqualTo(bank)
        verify(exactly = 1) { bankRepository.findByCodigo("290") }
    }

    @Test
    fun `findByCodigo returns null when repository returns null`() {
        every { bankRepository.findByCodigo("999") } returns null

        val result = adapter.findByCodigo("999")

        assertThat(result).isNull()
        verify(exactly = 1) { bankRepository.findByCodigo("999") }
    }

    @Test
    fun `findAllOrderByNomeAsc delegates to repository and returns list`() {
        val id = UUID.randomUUID()
        val banks =
            listOf(
                Bank(
                    id = id,
                    nome = "Banco A",
                    codigo = "001",
                    enderecoCompleto = "",
                    tipoIntegracao = "",
                    ativo = true,
                ),
            )
        every { bankRepository.findAllByOrderByNomeAsc() } returns banks

        val result = adapter.findAllOrderByNomeAsc()

        assertThat(result).isEqualTo(banks)
        verify(exactly = 1) { bankRepository.findAllByOrderByNomeAsc() }
    }
}
