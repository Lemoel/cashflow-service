package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

class BankRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: BankRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = BankRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findById returns null when query returns empty list`() {
        every {
            jdbcTemplate.query(any<String>(), any<PreparedStatementSetter>(), any<RowMapper<Bank>>())
        } returns emptyList()

        val id = UUID.randomUUID()
        val result = repository.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { jdbcTemplate.query(any<String>(), any<PreparedStatementSetter>(), any<RowMapper<Bank>>()) }
    }

    @Test
    fun `findById returns bank when query returns one item`() {
        val id = UUID.randomUUID()
        val bank = Bank(id = id, nome = "Banco A", codigo = "001", enderecoCompleto = "", tipoIntegracao = "", ativo = true)
        every {
            jdbcTemplate.query(any<String>(), any<PreparedStatementSetter>(), any<RowMapper<Bank>>())
        } returns listOf(bank)

        val result = repository.findById(id)

        assertThat(result).isEqualTo(bank)
        verify(exactly = 1) { jdbcTemplate.query(any<String>(), any<PreparedStatementSetter>(), any<RowMapper<Bank>>()) }
    }

    @Test
    fun `findAllByOrderByNomeAsc returns list from query and SQL contains ORDER BY nome ASC`() {
        val sqlSlot = slot<String>()
        val id = UUID.randomUUID()
        val banks =
            listOf(
                Bank(id = id, nome = "Banco A", codigo = "001", enderecoCompleto = "", tipoIntegracao = "", ativo = true),
            )
        every {
            jdbcTemplate.query(capture(sqlSlot), any<RowMapper<Bank>>())
        } returns banks

        val result = repository.findAllByOrderByNomeAsc()

        assertThat(result).isEqualTo(banks)
        assertThat(sqlSlot.captured).contains("ORDER BY nome ASC")
        verify(exactly = 1) { jdbcTemplate.query(any<String>(), any<RowMapper<Bank>>()) }
    }
}
