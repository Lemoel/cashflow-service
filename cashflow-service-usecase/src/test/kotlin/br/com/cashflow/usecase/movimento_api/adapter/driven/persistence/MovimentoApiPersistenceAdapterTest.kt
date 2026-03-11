package br.com.cashflow.usecase.movimento_api.adapter.driven.persistence

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.entity.StatusProcessamentoEnum
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MovimentoApiPersistenceAdapterTest {
    private val movimentoApiRepository: MovimentoApiRepository = mockk()
    private lateinit var adapter: MovimentoApiPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = MovimentoApiPersistenceAdapter(movimentoApiRepository)
    }

    @Test
    fun `save delegates to repository and returns saved entity`() {
        val id = UUID.randomUUID()
        val movimento =
            MovimentoApi(
                id = id,
                status = StatusProcessamentoEnum.RECEBIDO,
                creationUserId = "CRON",
            )
        every { movimentoApiRepository.save(any()) } returns movimento

        val result = adapter.save(movimento)

        assertThat(result).isEqualTo(movimento)
        verify(exactly = 1) { movimentoApiRepository.save(movimento) }
    }

    @Test
    fun `findFirstByOrderByDataLeituraDesc delegates to repository`() {
        val movimento =
            MovimentoApi(status = StatusProcessamentoEnum.PROCESSADA, dataLeitura = LocalDate.now())
        every { movimentoApiRepository.findFirstByOrderByDataLeituraDesc() } returns movimento

        val result = adapter.findFirstByOrderByDataLeituraDesc()

        assertThat(result).isEqualTo(movimento)
        verify(exactly = 1) { movimentoApiRepository.findFirstByOrderByDataLeituraDesc() }
    }

    @Test
    fun `findByDataLeituraAndPagina delegates to repository`() {
        val data = LocalDate.of(2025, 1, 15)
        val movimento = MovimentoApi(dataLeitura = data, pagina = 1)
        every { movimentoApiRepository.findByDataLeituraAndPagina(data, 1) } returns movimento

        val result = adapter.findByDataLeituraAndPagina(data, 1)

        assertThat(result).isEqualTo(movimento)
        verify(exactly = 1) { movimentoApiRepository.findByDataLeituraAndPagina(data, 1) }
    }
}
