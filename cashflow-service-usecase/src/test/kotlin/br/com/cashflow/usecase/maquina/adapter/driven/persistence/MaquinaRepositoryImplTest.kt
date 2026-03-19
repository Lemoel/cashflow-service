package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MaquinaRepositoryImplTest {
    private val entityManager: EntityManager = mockk()
    private val query: Query = mockk()
    private lateinit var repository: MaquinaRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MaquinaRepositoryImpl(entityManager)
    }

    @Test
    fun `findByIdWithDetalhes returns null when query returns empty list`() {
        val id = UUID.randomUUID()
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.resultList } returns emptyList<Array<Any?>>()

        val result = repository.findByIdWithDetalhes(id)

        assertThat(result).isNull()
        verify(exactly = 1) { entityManager.createNativeQuery(any<String>()) }
    }

    @Test
    fun `findByIdWithDetalhes returns item when query returns one row`() {
        val id = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val now = Instant.now()
        val row =
            arrayOf<Any?>(
                id.toString(),
                "ABC",
                congregacaoId.toString(),
                "Cong",
                bancoId.toString(),
                "Banco",
                null,
                null,
                true,
                1L,
                java.sql.Timestamp.from(now),
                null,
            )
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.resultList } returns listOf(row)

        val result = repository.findByIdWithDetalhes(id)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(id)
        assertThat(result.maquinaId).isEqualTo("ABC")
        assertThat(result.congregacaoNome).isEqualTo("Cong")
    }

    @Test
    fun `findWithFiltersComDetalhes with no filters uses count and select without WHERE`() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        val result = repository.findWithFiltersComDetalhes(null, null, null, null, 0, 10)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
    }

    @Test
    fun `findWithFiltersComDetalhes with maquinaId adds parameter`() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 1L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findWithFiltersComDetalhes("XYZ", null, null, null, 0, 10)

        verify(atLeast = 1) { query.setParameter("maquinaId", "%XYZ%") }
    }

    @Test
    fun `findParaSelecaoHistorico with no filters returns empty`() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        val result = repository.findParaSelecaoHistorico(null, null, null, 0, 10)

        assertThat(result.items).isEmpty()
        assertThat(result.total).isEqualTo(0L)
    }

    @Test
    fun `findParaSelecaoHistorico with tenantId adds parameter`() {
        val tenantId = UUID.randomUUID()
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findParaSelecaoHistorico(tenantId, null, null, 0, 10)

        verify(atLeast = 1) { query.setParameter("tenantId", tenantId) }
    }

    @Test
    fun `findWithFiltersComDetalhes with congregacao adds parameter`() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findWithFiltersComDetalhes(null, "Cong A", null, null, 0, 10)

        verify(atLeast = 1) { query.setParameter("congregacao", "%Cong A%") }
    }

    @Test
    fun `findWithFiltersComDetalhes with departamentoId adds parameter`() {
        val deptoId = UUID.randomUUID()
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findWithFiltersComDetalhes(null, null, null, deptoId, 0, 10)

        verify(atLeast = 1) { query.setParameter("departamentoId", deptoId) }
    }

    @Test
    fun `findParaSelecaoHistorico with congregacaoId adds parameter`() {
        val congregacaoId = UUID.randomUUID()
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findParaSelecaoHistorico(null, congregacaoId, null, 0, 10)

        verify(atLeast = 1) { query.setParameter("congregacaoId", congregacaoId) }
    }

    @Test
    fun `findParaSelecaoHistorico with numeroSerieLeitor adds parameter`() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.singleResult } returns 0L
        every { query.resultList } returns emptyList<Array<Any?>>()

        repository.findParaSelecaoHistorico(null, null, "ABC", 0, 10)

        verify(atLeast = 1) { query.setParameter("numeroSerieLeitor", "%ABC%") }
    }
}
