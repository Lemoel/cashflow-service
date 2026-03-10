package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val bank = Bank(id = id, nome = "Banco A", codigo = "001", enderecoCompleto = "", tipoIntegracao = "", ativo = true)
        every { bankRepository.findById(id) } returns bank

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(bank)
        verify(exactly = 1) { bankRepository.findById(id) }
    }

    @Test
    fun `findById returns null when repository returns null`() {
        val id = UUID.randomUUID()
        every { bankRepository.findById(id) } returns null

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { bankRepository.findById(id) }
    }

    @Test
    fun `findAllOrderByNomeAsc delegates to repository and returns list`() {
        val id = UUID.randomUUID()
        val banks =
            listOf(
                Bank(id = id, nome = "Banco A", codigo = "001", enderecoCompleto = "", tipoIntegracao = "", ativo = true),
            )
        every { bankRepository.findAllByOrderByNomeAsc() } returns banks

        val result = adapter.findAllOrderByNomeAsc()

        assertThat(result).isEqualTo(banks)
        verify(exactly = 1) { bankRepository.findAllByOrderByNomeAsc() }
    }
}
