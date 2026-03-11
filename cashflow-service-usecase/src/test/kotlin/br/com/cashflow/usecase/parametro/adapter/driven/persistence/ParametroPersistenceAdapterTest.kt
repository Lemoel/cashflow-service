package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class ParametroPersistenceAdapterTest {
    private val parametroRepository: ParametroRepository = mockk()
    private lateinit var adapter: ParametroPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = ParametroPersistenceAdapter(parametroRepository)
    }

    @Test
    fun `save delegates to repository and returns saved parametro`() {
        val parametro = Parametro(chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)
        val saved =
            Parametro(
                id = UUID.randomUUID(),
                chave = "K",
                valorTexto = "v",
                tipo = "STRING",
                ativo = true,
            )
        every { parametroRepository.save(parametro) } returns saved

        val result = adapter.save(parametro)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { parametroRepository.save(parametro) }
    }

    @Test
    fun `findById delegates to repository and returns entity when found`() {
        val id = UUID.randomUUID()
        val parametro =
            Parametro(id = id, chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)
        every { parametroRepository.findById(id) } returns Optional.of(parametro)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(parametro)
        verify(exactly = 1) { parametroRepository.findById(id) }
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { parametroRepository.findById(id) } returns Optional.empty()

        assertThat(adapter.findById(id)).isNull()
        verify(exactly = 1) { parametroRepository.findById(id) }
    }

    @Test
    fun `findWithFilters delegates to findWithFilters and returns ParametroPage`() {
        val pageable = PageRequest.of(0, 10)
        val items =
            listOf(
                Parametro(
                    id = UUID.randomUUID(),
                    chave = "A",
                    valorTexto = "1",
                    tipo = "STRING",
                    ativo = true,
                ),
            )
        val springPage = PageImpl(items, pageable, 1L)
        every { parametroRepository.findWithFilters(null, pageable) } returns springPage

        val result = adapter.findWithFilters(null, 0, 10)

        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
        verify(exactly = 1) { parametroRepository.findWithFilters(null, pageable) }
    }

    @Test
    fun `findWithFilters with filter delegates with filter`() {
        val pageable = PageRequest.of(1, 5)
        val filter = ParametroFilterModel(chave = "X", ativo = true)
        val springPage = PageImpl(emptyList<Parametro>(), pageable, 0L)
        every { parametroRepository.findWithFilters(filter, pageable) } returns springPage

        val result = adapter.findWithFilters(filter, 1, 5)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
        assertThat(result.page).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(5)
        verify(exactly = 1) { parametroRepository.findWithFilters(filter, pageable) }
    }

    @Test
    fun `findAllOrderByChave delegates to findAllByOrderByChaveAsc`() {
        val list =
            listOf(
                Parametro(
                    id = UUID.randomUUID(),
                    chave = "A",
                    valorTexto = "1",
                    tipo = "STRING",
                    ativo = true,
                ),
            )
        every { parametroRepository.findAllByOrderByChaveAsc() } returns list

        val result = adapter.findAllOrderByChave()

        assertThat(result).isEqualTo(list)
        verify(exactly = 1) { parametroRepository.findAllByOrderByChaveAsc() }
    }

    @Test
    fun `existsByChave delegates to repository`() {
        every { parametroRepository.existsByChave("K") } returns true

        val result = adapter.existsByChave("K")

        assertThat(result).isTrue()
        verify(exactly = 1) { parametroRepository.existsByChave("K") }
    }

    @Test
    fun `existsByChaveExcludingId when excludeId null delegates to existsByChave`() {
        every { parametroRepository.existsByChave("K") } returns false

        val result = adapter.existsByChaveExcludingId("K", null)

        assertThat(result).isFalse()
        verify(exactly = 1) { parametroRepository.existsByChave("K") }
    }

    @Test
    fun `existsByChaveExcludingId when excludeId set delegates to existsByChaveAndIdNot`() {
        val id = UUID.randomUUID()
        every { parametroRepository.existsByChaveAndIdNot("K", id) } returns true

        val result = adapter.existsByChaveExcludingId("K", id)

        assertThat(result).isTrue()
        verify(exactly = 1) { parametroRepository.existsByChaveAndIdNot("K", id) }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { parametroRepository.deleteById(id) } just runs

        adapter.deleteById(id)

        verify(exactly = 1) { parametroRepository.deleteById(id) }
    }
}
