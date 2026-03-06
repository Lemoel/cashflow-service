package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationFilter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

class CongregationRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: CongregationRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = CongregationRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findFiltered with null filter uses count and select without WHERE and params pageSize offset`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 0L
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Congregation>>(), *anyVararg()) } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0L)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(countSqlSlot.captured).contains("SELECT COUNT(*) FROM eventos.congregacao c")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("SELECT * FROM eventos.congregacao c")
        assertThat(selectSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("ORDER BY c.nome ASC LIMIT ? OFFSET ?")
        verify(exactly = 1) { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) }
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<Congregation>>(), *anyVararg()) }
    }

    @Test
    fun `findFiltered with nome filter uses WHERE c nome and correct params`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 2L
        val congregations = listOf(
            Congregation(nome = "Cong A", logradouro = "", bairro = "", numero = "", cidade = "", uf = "", cep = ""),
            Congregation(nome = "Cong A", logradouro = "", bairro = "", numero = "", cidade = "", uf = "", cep = ""),
        )
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Congregation>>(), *anyVararg()) } returns congregations

        val pageable = PageRequest.of(0, 10)
        val filter = CongregationFilter(nome = "Cong A", cnpj = null, ativo = null)
        val result = repository.findFiltered(filter, pageable)

        assertThat(result.content).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2L)
        assertThat(countSqlSlot.captured).contains("WHERE")
        assertThat(countSqlSlot.captured).contains("c.nome = ?")
        assertThat(selectSqlSlot.captured).contains("WHERE")
        assertThat(selectSqlSlot.captured).contains("c.nome = ?")
        assertThat(selectSqlSlot.captured).contains("ORDER BY c.nome ASC LIMIT ? OFFSET ?")
    }

    @Test
    fun `findFiltered with nome cnpj and ativo uses all conditions`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Congregation>>(), *anyVararg()) } returns emptyList()

        val pageable = PageRequest.of(1, 5)
        val filter = CongregationFilter(nome = "Cong B", cnpj = "12345678000199", ativo = true)
        val result = repository.findFiltered(filter, pageable)

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(result.pageable.pageNumber).isEqualTo(1)
        assertThat(result.pageable.pageSize).isEqualTo(5)
        assertThat(countSqlSlot.captured).contains("c.nome = ?")
        assertThat(countSqlSlot.captured).contains("c.cnpj = ?")
        assertThat(countSqlSlot.captured).contains("c.ativo = ?")
        assertThat(selectSqlSlot.captured).contains("c.nome = ?")
        assertThat(selectSqlSlot.captured).contains("c.cnpj = ?")
        assertThat(selectSqlSlot.captured).contains("c.ativo = ?")
    }

    @Test
    fun `findFiltered returns PageImpl with content from query and total from queryForObject`() {
        val singleCongregation = listOf(
            Congregation(id = UUID.randomUUID(), nome = "Only", logradouro = "", bairro = "", numero = "", cidade = "", uf = "", cep = ""),
        )
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(any(), any<RowMapper<Congregation>>(), *anyVararg()) } returns singleCongregation

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result).isInstanceOf(PageImpl::class.java)
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].nome).isEqualTo("Only")
        assertThat(result.totalElements).isEqualTo(1L)
    }
}
