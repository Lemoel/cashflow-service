package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
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

class CongregationPersistenceAdapterTest {
    private val congregationRepository: CongregationRepository = mockk()
    private lateinit var adapter: CongregationPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = CongregationPersistenceAdapter(congregationRepository)
    }

    @Test
    fun `save delegates to repository and returns saved congregation`() {
        val congregation =
            Congregation(
                id = null,
                nome = "Cong A",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "SP",
                uf = "SP",
                cep = "01234567",
            )
        val saved =
            Congregation(
                id = UUID.randomUUID(),
                nome = "Cong A",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "SP",
                uf = "SP",
                cep = "01234567",
            )
        every { congregationRepository.save(congregation) } returns saved

        val result = adapter.save(congregation)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { congregationRepository.save(congregation) }
    }

    @Test
    fun `findById delegates to repository and returns entity when found`() {
        val id = UUID.randomUUID()
        val congregation =
            Congregation(
                id = id,
                nome = "Cong A",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "SP",
                uf = "SP",
                cep = "01234567",
            )
        every { congregationRepository.findById(id) } returns Optional.of(congregation)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(congregation)
        verify(exactly = 1) { congregationRepository.findById(id) }
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { congregationRepository.findById(id) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { congregationRepository.findById(id) }
    }

    @Test
    fun `findAll delegates to findFiltered and returns CongregationPage`() {
        val pageable = PageRequest.of(0, 10)
        val congregations =
            listOf(
                Congregation(
                    id = UUID.randomUUID(),
                    nome = "Cong A",
                    logradouro = "Rua X",
                    bairro = "Centro",
                    numero = "1",
                    cidade = "SP",
                    uf = "SP",
                    cep = "01234567",
                ),
            )
        val springPage = PageImpl(congregations, pageable, 1L)
        every { congregationRepository.findFiltered(null, pageable) } returns springPage

        val result = adapter.findAll(null, 0, 10)

        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
        verify(exactly = 1) { congregationRepository.findFiltered(null, pageable) }
    }

    @Test
    fun `findAll with filter delegates to findFiltered with filter`() {
        val pageable = PageRequest.of(1, 5)
        val filter = CongregationFilterModel(nome = "Cong A", cnpj = null, ativo = true)
        val congregations = emptyList<Congregation>()
        val springPage = PageImpl(congregations, pageable, 0L)
        every { congregationRepository.findFiltered(filter, pageable) } returns springPage

        val result = adapter.findAll(filter, 1, 5)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
        assertThat(result.page).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(5)
        verify(exactly = 1) { congregationRepository.findFiltered(filter, pageable) }
    }

    @Test
    fun `findAllOrderByNome delegates to findAllProjectedByOrderByNomeAsc and maps to pairs`() {
        val id = UUID.randomUUID()
        val projection = mockk<CongregationIdNameProjection>()
        every { projection.getId() } returns id
        every { projection.getNome() } returns "Cong A"
        every { congregationRepository.findAllProjectedByOrderByNomeAsc() } returns listOf(projection)

        val result = adapter.findAllOrderByNome()

        assertThat(result).containsExactly(id to "Cong A")
        verify(exactly = 1) { congregationRepository.findAllProjectedByOrderByNomeAsc() }
    }

    @Test
    fun `findSetoriais delegates to findProjectedBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc and maps to pairs`() {
        val id = UUID.randomUUID()
        val projection = mockk<CongregationIdNameProjection>()
        every { projection.getId() } returns id
        every { projection.getNome() } returns "Setorial"
        every {
            congregationRepository.findProjectedBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc()
        } returns listOf(projection)

        val result = adapter.findSetoriais()

        assertThat(result).containsExactly(id to "Setorial")
        verify(exactly = 1) {
            congregationRepository.findProjectedBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc()
        }
    }

    @Test
    fun `existsByCnpjExcludingId returns repository result when excludeId is null`() {
        every { congregationRepository.existsByCnpj("12345678000199") } returns true

        val result = adapter.existsByCnpjExcludingId("12345678000199", null)

        assertThat(result).isTrue()
        verify(exactly = 1) { congregationRepository.existsByCnpj("12345678000199") }
    }

    @Test
    fun `existsByCnpjExcludingId returns repository result when excludeId is set`() {
        val id = UUID.randomUUID()
        every { congregationRepository.existsByCnpjAndIdNot("12345678000199", id) } returns false

        val result = adapter.existsByCnpjExcludingId("12345678000199", id)

        assertThat(result).isFalse()
        verify(exactly = 1) { congregationRepository.existsByCnpjAndIdNot("12345678000199", id) }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { congregationRepository.deleteById(id) } just runs

        adapter.deleteById(id)

        verify(exactly = 1) { congregationRepository.deleteById(id) }
    }
}
