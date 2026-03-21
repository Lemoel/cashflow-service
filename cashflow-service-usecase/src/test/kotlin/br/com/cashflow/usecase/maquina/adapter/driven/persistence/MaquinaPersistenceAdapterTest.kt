package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class MaquinaPersistenceAdapterTest {
    private val maquinaRepository: MaquinaRepository = mockk()
    private lateinit var adapter: MaquinaPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = MaquinaPersistenceAdapter(maquinaRepository)
    }

    @Test
    fun `save delegates to repository and returns saved maquina`() {
        val maquina =
            Maquina(
                numeroSerieLeitor = "ABC",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )
        val savedId = UUID.randomUUID()
        val saved =
            Maquina(
                id = savedId,
                numeroSerieLeitor = "ABC",
                congregacaoId = maquina.congregacaoId,
                bancoId = maquina.bancoId,
            )
        every { maquinaRepository.save(maquina) } returns saved
        every { maquinaRepository.flush() } just runs

        val result = adapter.save(maquina)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { maquinaRepository.save(maquina) }
        verify(exactly = 1) { maquinaRepository.flush() }
    }

    @Test
    fun `saveAll returns empty without save or flush when list is empty`() {
        val result = adapter.saveAll(emptyList())

        assertThat(result).isEmpty()
        verify(exactly = 0) { maquinaRepository.saveAll(match<List<Maquina>> { true }) }
        verify(exactly = 0) { maquinaRepository.flush() }
    }

    @Test
    fun `saveAll flushes after persisting maquinas`() {
        val m =
            Maquina(
                id = UUID.randomUUID(),
                numeroSerieLeitor = "S1",
                bancoId = UUID.randomUUID(),
            )
        every { maquinaRepository.saveAll(listOf(m)) } returns listOf(m)
        every { maquinaRepository.flush() } just runs

        val result = adapter.saveAll(listOf(m))

        assertThat(result).containsExactly(m)
        verify(exactly = 1) { maquinaRepository.saveAll(listOf(m)) }
        verify(exactly = 1) { maquinaRepository.flush() }
    }

    @Test
    fun `findById returns entity when repository returns Optional with value`() {
        val id = UUID.randomUUID()
        val maquina =
            Maquina(
                id = id,
                numeroSerieLeitor = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )
        every { maquinaRepository.findById(id) } returns Optional.of(maquina)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(maquina)
        verify(exactly = 1) { maquinaRepository.findById(id) }
    }

    @Test
    fun `findById returns null when repository returns empty Optional`() {
        val id = UUID.randomUUID()
        every { maquinaRepository.findById(id) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { maquinaRepository.findById(id) }
    }

    @Test
    fun `findByIdWithDetalhes delegates to repository`() {
        val id = UUID.randomUUID()
        val dto =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "X",
                congregacaoId = UUID.randomUUID(),
                congregacaoNome = "Cong",
                bancoId = UUID.randomUUID(),
                bancoNome = "B",
                departamentoId = null,
                departamentoNome = null,
                ativo = true,
                version = null,
                createdAt = null,
                updatedAt = null,
            )
        every { maquinaRepository.findByIdWithDetalhes(id) } returns dto

        val result = adapter.findByIdWithDetalhes(id)

        assertThat(result).isEqualTo(dto)
        verify(exactly = 1) { maquinaRepository.findByIdWithDetalhes(id) }
    }

    @Test
    fun `existsByNumeroSerieLeitor delegates to repository and returns true when exists`() {
        every { maquinaRepository.existsByNumeroSerieLeitor("ABC") } returns true

        val result = adapter.existsByNumeroSerieLeitor("ABC")

        assertThat(result).isTrue()
        verify(exactly = 1) { maquinaRepository.existsByNumeroSerieLeitor("ABC") }
    }

    @Test
    fun `existsByNumeroSerieLeitor returns false when repository returns false`() {
        every { maquinaRepository.existsByNumeroSerieLeitor("XYZ") } returns false

        val result = adapter.existsByNumeroSerieLeitor("XYZ")

        assertThat(result).isFalse()
        verify(exactly = 1) { maquinaRepository.existsByNumeroSerieLeitor("XYZ") }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { maquinaRepository.deleteById(id) } just runs

        adapter.deleteById(id)

        verify(exactly = 1) { maquinaRepository.deleteById(id) }
    }

    @Test
    fun `findWithFiltersComDetalhes delegates and maps to MaquinaPage`() {
        val queryResult = MaquinaQueryResult(items = emptyList(), total = 0L)
        every {
            maquinaRepository.findWithFiltersComDetalhes(null, null, null, null, 0, 10)
        } returns queryResult

        val result = adapter.findWithFiltersComDetalhes(null, null, null, null, 0, 10)

        assertThat(
            result,
        ).isEqualTo(MaquinaPage(items = emptyList(), total = 0L, page = 0, pageSize = 10))
        verify(
            exactly = 1,
        ) { maquinaRepository.findWithFiltersComDetalhes(null, null, null, null, 0, 10) }
    }

    @Test
    fun `findParaSelecaoHistorico delegates and maps to MaquinaPage`() {
        val queryResult = MaquinaQueryResult(items = emptyList(), total = 0L)
        every {
            maquinaRepository.findParaSelecaoHistorico(null, null, null, 0, 20)
        } returns queryResult

        val result = adapter.findParaSelecaoHistorico(null, null, null, 0, 20)

        assertThat(
            result,
        ).isEqualTo(MaquinaPage(items = emptyList(), total = 0L, page = 0, pageSize = 20))
        verify(exactly = 1) { maquinaRepository.findParaSelecaoHistorico(null, null, null, 0, 20) }
    }
}
