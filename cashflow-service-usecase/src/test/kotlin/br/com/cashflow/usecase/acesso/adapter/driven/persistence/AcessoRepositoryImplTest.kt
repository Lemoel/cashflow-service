package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.time.Instant
import java.util.UUID

class AcessoRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: AcessoRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AcessoRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findFiltered with null filter returns all items without WHERE`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result).isEmpty()
        assertThat(selectSqlSlot.captured).doesNotContain("WHERE")
        assertThat(selectSqlSlot.captured).contains("ORDER BY a.nome ASC")
        assertThat(selectSqlSlot.captured).contains("LIMIT ? OFFSET ?")
    }

    @Test
    fun `findFiltered with email filter uses ILIKE condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter = AcessoFilter(email = "user@test")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).contains("a.email ILIKE ?")
    }

    @Test
    fun `findFiltered with congregacaoId filter uses congregacao condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val congId = UUID.randomUUID()
        val filter = AcessoFilter(congregacaoId = congId)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).contains("ac.congregacao_id = ?")
    }

    @Test
    fun `findFiltered with perfil filter uses tipo_acesso condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter = AcessoFilter(perfil = "ADMIN")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).contains("a.tipo_acesso = ?")
    }

    @Test
    fun `findFiltered with ativo filter uses ativo condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter = AcessoFilter(ativo = true)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).contains("a.ativo = ?")
    }

    @Test
    fun `findFiltered with all filters uses all conditions`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter =
            AcessoFilter(
                email = "user",
                congregacaoId = UUID.randomUUID(),
                perfil = "ADMIN",
                ativo = false,
            )
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).contains("a.email ILIKE ?")
        assertThat(selectSqlSlot.captured).contains("ac.congregacao_id = ?")
        assertThat(selectSqlSlot.captured).contains("a.tipo_acesso = ?")
        assertThat(selectSqlSlot.captured).contains("a.ativo = ?")
    }

    @Test
    fun `findFiltered returns items from query`() {
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = "USER",
                telefone = "11999999999",
                tipoAcesso = "ADMIN",
                ativo = true,
                data = Instant.now(),
                modDateTime = null,
                congregacaoId = UUID.randomUUID(),
                congregacaoNome = "Congregacao A",
            )
        every {
            jdbcTemplate.query(
                any<String>(),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns listOf(item)

        val result = repository.findFiltered(null, PageRequest.of(0, 10))

        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("user@test.com")
    }

    @Test
    fun `countFiltered with null filter returns count without WHERE`() {
        val countSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns 5L

        val result = repository.countFiltered(null)

        assertThat(result).isEqualTo(5L)
        assertThat(countSqlSlot.captured).contains("COUNT(DISTINCT a.email)")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
    }

    @Test
    fun `countFiltered with filter applies conditions`() {
        val countSqlSlot = slot<String>()
        every {
            jdbcTemplate.queryForObject(
                capture(countSqlSlot),
                Long::class.java,
                *anyVararg(),
            )
        } returns 2L

        val filter = AcessoFilter(email = "user", ativo = true)
        val result = repository.countFiltered(filter)

        assertThat(result).isEqualTo(2L)
        assertThat(countSqlSlot.captured).contains("a.email ILIKE ?")
        assertThat(countSqlSlot.captured).contains("a.ativo = ?")
    }

    @Test
    fun `countFiltered returns 0 when queryForObject returns null`() {
        every {
            jdbcTemplate.queryForObject(
                any<String>(),
                Long::class.java,
                *anyVararg(),
            )
        } returns null

        val result = repository.countFiltered(null)

        assertThat(result).isEqualTo(0L)
    }

    @Test
    fun `findFiltered with blank email filter does not add email condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter = AcessoFilter(email = "  ")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).doesNotContain("a.email ILIKE ?")
    }

    @Test
    fun `findFiltered with blank perfil filter does not add perfil condition`() {
        val selectSqlSlot = slot<String>()
        every {
            jdbcTemplate.query(
                capture(selectSqlSlot),
                any<RowMapper<AcessoListItem>>(),
                *anyVararg(),
            )
        } returns emptyList()

        val filter = AcessoFilter(perfil = "")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        assertThat(selectSqlSlot.captured).doesNotContain("a.tipo_acesso = ?")
    }
}
