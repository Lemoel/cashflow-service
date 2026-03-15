package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
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

class ParametroRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: ParametroRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = ParametroRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findWithFilters with null filter uses count and select without WHERE`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Parametro>>(), *anyVararg())
        } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findWithFilters(null, pageable)

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0L)
        assertThat(countSqlSlot.captured).contains("SELECT COUNT(*) FROM parametro p")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("SELECT * FROM parametro p")
        assertThat(selectSqlSlot.captured).contains("ORDER BY p.chave ASC LIMIT ? OFFSET ?")
        verify(exactly = 1) { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) }
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<Parametro>>(), *anyVararg()) }
    }

    @Test
    fun `findWithFilters with chave filter uses ILIKE`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            1L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Parametro>>(), *anyVararg())
        } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val filter = ParametroFilterModel(chave = "KEY", ativo = null)
        repository.findWithFilters(filter, pageable)

        assertThat(countSqlSlot.captured).contains("p.chave ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("p.chave ILIKE ?")
    }

    @Test
    fun `findWithFilters with ativo filter uses ativo condition`() {
        val countSqlSlot = slot<String>()
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns
            0L
        every {
            jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<Parametro>>(), *anyVararg())
        } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val filter = ParametroFilterModel(chave = null, ativo = true)
        repository.findWithFilters(filter, pageable)

        assertThat(countSqlSlot.captured).contains("p.ativo = ?")
        assertThat(selectSqlSlot.captured).contains("p.ativo = ?")
    }

    @Test
    fun `findWithFilters returns PageImpl with content and total`() {
        val items =
            listOf(
                Parametro(
                    id = UUID.randomUUID(),
                    chave = "A",
                    valorTexto = "v",
                    tipo = "STRING",
                    ativo = true,
                ),
            )
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 1L
        every { jdbcTemplate.query(any(), any<RowMapper<Parametro>>(), *anyVararg()) } returns items

        val pageable = PageRequest.of(0, 10)
        val result = repository.findWithFilters(null, pageable)

        assertThat(result).isInstanceOf(PageImpl::class.java)
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].chave).isEqualTo("A")
        assertThat(result.totalElements).isEqualTo(1L)
    }
}
