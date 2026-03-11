package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import java.time.Instant
import java.util.UUID

class MaquinaHistoricoRepositoryImplTest {
    private val jdbcTemplate: JdbcTemplate = mockk()
    private lateinit var repository: MaquinaHistoricoRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MaquinaHistoricoRepositoryImpl(jdbcTemplate)
    }

    @Test
    fun `findByMaquinaIdOrderByDataInicioDesc returns list from query`() {
        val maquinaId = UUID.randomUUID()
        val row =
            MaquinaHistoricoItemRow(
                id = UUID.randomUUID(),
                maquinaId = maquinaId,
                congregacaoId = UUID.randomUUID(),
                congregacaoNome = "Cong",
                departamentoId = null,
                departamentoNome = null,
                dataInicio = Instant.now(),
                dataFim = null,
            )
        every {
            jdbcTemplate.query(
                any<String>(),
                any<PreparedStatementSetter>(),
                any<RowMapper<MaquinaHistoricoItemRow>>(),
            )
        } returns listOf(row)

        val result = repository.findByMaquinaIdOrderByDataInicioDesc(maquinaId)

        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(row)
        verify(exactly = 1) {
            jdbcTemplate.query(
                any<String>(),
                any<PreparedStatementSetter>(),
                any<RowMapper<MaquinaHistoricoItemRow>>(),
            )
        }
    }
}
