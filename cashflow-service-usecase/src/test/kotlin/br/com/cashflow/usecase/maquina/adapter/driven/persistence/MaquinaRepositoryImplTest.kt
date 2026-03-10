package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.time.Instant
import java.util.UUID

class MaquinaRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: MaquinaRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MaquinaRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findByIdWithDetalhes returns null when query returns empty list`() {
        val id = UUID.randomUUID()
        every { jdbcTemplate.query(any(), any<RowMapper<MaquinaComCongregacao>>(), any()) } returns emptyList()

        val result = repository.findByIdWithDetalhes(id)

        assertThat(result).isNull()
        verify(exactly = 1) { jdbcTemplate.query(any(), any<RowMapper<MaquinaComCongregacao>>(), any()) }
    }

    @Test
    fun `findByIdWithDetalhes returns item when query returns one`() {
        val id = UUID.randomUUID()
        val item =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "ABC",
                congregacaoId = UUID.randomUUID(),
                congregacaoNome = "Cong",
                bancoId = UUID.randomUUID(),
                bancoNome = "Banco",
                departamentoId = null,
                departamentoNome = null,
                ativo = true,
                version = 1L,
                createdAt = Instant.now(),
                updatedAt = null,
            )
        every { jdbcTemplate.query(any(), any<RowMapper<MaquinaComCongregacao>>(), any()) } returns listOf(item)

        val result = repository.findByIdWithDetalhes(id)

        assertThat(result).isEqualTo(item)
    }

    @Test
    fun `findWithFiltersComDetalhes with no filters uses count and select without WHERE`() {
        val countSqlSlot = slot<String>()
        every { jdbcTemplate.queryForObject(capture(countSqlSlot), Long::class.java, *anyVararg()) } returns 0L
        every { jdbcTemplate.query(any(), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        val result = repository.findWithFiltersComDetalhes(null, null, null, null, 0, 10)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
        assertThat(countSqlSlot.captured).contains("SELECT COUNT(*)")
        assertThat(countSqlSlot.captured).contains("FROM eventos.maquina m")
        assertThat(countSqlSlot.captured).doesNotContain("WHERE")
    }

    @Test
    fun `findWithFiltersComDetalhes with maquinaId adds ILIKE condition`() {
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 1L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findWithFiltersComDetalhes("XYZ", null, null, null, 0, 10)

        assertThat(selectSqlSlot.captured).contains("numero_serie_leitor")
        assertThat(selectSqlSlot.captured).contains("ILIKE")
    }

    @Test
    fun `findParaSelecaoHistorico with no filters uses count and select without WHERE`() {
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        every { jdbcTemplate.query(any(), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        val result = repository.findParaSelecaoHistorico(null, null, null, 0, 10)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
    }

    @Test
    fun `findParaSelecaoHistorico with tenantId adds tenant_id condition`() {
        val tenantId = UUID.randomUUID()
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findParaSelecaoHistorico(tenantId, null, null, 0, 10)

        assertThat(selectSqlSlot.captured).contains("tenant_id")
    }

    @Test
    fun `findWithFiltersComDetalhes with congregacao adds c nome ILIKE`() {
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findWithFiltersComDetalhes(null, "Cong A", null, null, 0, 10)

        assertThat(selectSqlSlot.captured).contains("c.nome")
        assertThat(selectSqlSlot.captured).contains("ILIKE")
    }

    @Test
    fun `findWithFiltersComDetalhes with departamentoId adds departamento_id condition`() {
        val deptoId = UUID.randomUUID()
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findWithFiltersComDetalhes(null, null, null, deptoId, 0, 10)

        assertThat(selectSqlSlot.captured).contains("departamento_id")
    }

    @Test
    fun `findParaSelecaoHistorico with congregacaoId adds congregacao_id condition`() {
        val congregacaoId = UUID.randomUUID()
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findParaSelecaoHistorico(null, congregacaoId, null, 0, 10)

        assertThat(selectSqlSlot.captured).contains("congregacao_id")
    }

    @Test
    fun `findParaSelecaoHistorico with numeroSerieLeitor adds numero_serie_leitor ILIKE`() {
        every { jdbcTemplate.queryForObject(any(), Long::class.java, *anyVararg()) } returns 0L
        val selectSqlSlot = slot<String>()
        every { jdbcTemplate.query(capture(selectSqlSlot), any<RowMapper<MaquinaComCongregacao>>(), *anyVararg()) } returns emptyList()

        repository.findParaSelecaoHistorico(null, null, "ABC", 0, 10)

        assertThat(selectSqlSlot.captured).contains("numero_serie_leitor")
        assertThat(selectSqlSlot.captured).contains("ILIKE")
    }
}
