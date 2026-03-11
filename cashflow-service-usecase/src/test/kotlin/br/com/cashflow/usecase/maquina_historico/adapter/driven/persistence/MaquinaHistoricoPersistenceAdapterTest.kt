package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import br.com.cashflow.usecase.maquina_historico.entity.MaquinaHistorico
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MaquinaHistoricoPersistenceAdapterTest {
    private val maquinaHistoricoRepository: MaquinaHistoricoRepository = mockk()
    private lateinit var adapter: MaquinaHistoricoPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = MaquinaHistoricoPersistenceAdapter(maquinaHistoricoRepository)
    }

    @Test
    fun `listarPorMaquinaId returns items mapped from repository rows`() {
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
        every { maquinaHistoricoRepository.findByMaquinaIdOrderByDataInicioDesc(maquinaId) } returns
            listOf(row)

        val result = adapter.listarPorMaquinaId(maquinaId)

        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(
            MaquinaHistoricoItemModel(
                id = row.id,
                maquinaId = row.maquinaId,
                congregacaoId = row.congregacaoId,
                congregacaoNome = row.congregacaoNome,
                departamentoId = row.departamentoId,
                departamentoNome = row.departamentoNome,
                dataInicio = row.dataInicio,
                dataFim = row.dataFim,
            ),
        )
        verify(
            exactly = 1,
        ) { maquinaHistoricoRepository.findByMaquinaIdOrderByDataInicioDesc(maquinaId) }
    }

    @Test
    fun `fecharPeriodoAtual delegates to repository`() {
        val maquinaId = UUID.randomUUID()
        every { maquinaHistoricoRepository.fecharPeriodoAtual(maquinaId) } just runs

        adapter.fecharPeriodoAtual(maquinaId)

        verify(exactly = 1) { maquinaHistoricoRepository.fecharPeriodoAtual(maquinaId) }
    }

    @Test
    fun `inserirPeriodo calls save with entity with correct maquinaId congregacaoId departamentoId dataInicio not null dataFim null`() {
        val maquinaId = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val departamentoId = UUID.randomUUID()
        val entitySlot = slot<MaquinaHistorico>()
        every { maquinaHistoricoRepository.save(capture(entitySlot)) } answers { firstArg() }

        adapter.inserirPeriodo(maquinaId, congregacaoId, departamentoId)

        val saved = entitySlot.captured
        assertThat(saved.id).isNotNull()
        assertThat(saved.maquinaId).isEqualTo(maquinaId)
        assertThat(saved.congregacaoId).isEqualTo(congregacaoId)
        assertThat(saved.departamentoId).isEqualTo(departamentoId)
        assertThat(saved.dataInicio).isNotNull()
        assertThat(saved.dataFim).isNull()
        verify(exactly = 1) { maquinaHistoricoRepository.save(any()) }
    }
}
