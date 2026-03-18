package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import java.util.UUID

class AcessoRepositoryImplTest {
    private val entityManager: EntityManager = mockk()
    private val listQuery: Query = mockk()
    private val countQuery: Query = mockk()
    private lateinit var repository: AcessoRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AcessoRepositoryImpl(entityManager)
    }

    @Test
    fun `findFiltered with null filter returns all items without WHERE`() {
        val sqlSlot = slot<String>()
        every { entityManager.createNativeQuery(capture(sqlSlot)) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val pageable = PageRequest.of(0, 10)
        val result = repository.findFiltered(null, pageable)

        assertThat(result).isEmpty()
        assertThat(sqlSlot.captured).doesNotContain("WHERE")
        assertThat(sqlSlot.captured).contains("ORDER BY a.nome ASC")
        assertThat(sqlSlot.captured).contains("LIMIT :limit OFFSET :offset")
    }

    @Test
    fun `findFiltered with email filter uses ILIKE condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val filter = AcessoFilter(email = "user@test")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(atLeast = 1) { listQuery.setParameter("email", "%user@test%") }
    }

    @Test
    fun `findFiltered with congregacaoId filter uses congregacao condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val congId = UUID.randomUUID()
        val filter = AcessoFilter(congregacaoId = congId)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(atLeast = 1) { listQuery.setParameter("congregacaoId", congId) }
    }

    @Test
    fun `findFiltered with perfil filter uses tipo_acesso condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val filter = AcessoFilter(perfil = "ADMIN")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(atLeast = 1) { listQuery.setParameter("perfil", "ADMIN") }
    }

    @Test
    fun `findFiltered with ativo filter uses ativo condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val filter = AcessoFilter(ativo = true)
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(atLeast = 1) { listQuery.setParameter("ativo", true) }
    }

    @Test
    fun `findFiltered returns items from query`() {
        val email = "user@test.com"
        val nome = "USER"
        val row = arrayOf<Any?>(email, nome, "11999999999", "ADMIN", true, null, null, UUID.randomUUID().toString(), "Congregacao A")
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns listOf(row)

        val result = repository.findFiltered(null, PageRequest.of(0, 10))

        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo(email)
        assertThat(result[0].nome).isEqualTo(nome)
    }

    @Test
    fun `countFiltered with null filter returns count without WHERE`() {
        every { entityManager.createNativeQuery(any()) } returns countQuery
        every { countQuery.setParameter(any<String>(), any()) } returns countQuery
        every { countQuery.singleResult } returns 5L

        val result = repository.countFiltered(null)

        assertThat(result).isEqualTo(5L)
    }

    @Test
    fun `countFiltered with filter applies conditions`() {
        every { entityManager.createNativeQuery(any()) } returns countQuery
        every { countQuery.setParameter(any<String>(), any()) } returns countQuery
        every { countQuery.singleResult } returns 2L

        val filter = AcessoFilter(email = "user", ativo = true)
        val result = repository.countFiltered(filter)

        assertThat(result).isEqualTo(2L)
    }

    @Test
    fun `countFiltered returns 0 when singleResult returns null`() {
        every { entityManager.createNativeQuery(any()) } returns countQuery
        every { countQuery.setParameter(any<String>(), any()) } returns countQuery
        every { countQuery.singleResult } returns null

        val result = repository.countFiltered(null)

        assertThat(result).isEqualTo(0L)
    }

    @Test
    fun `findFiltered with blank email filter does not add email condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val filter = AcessoFilter(email = "  ")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(exactly = 2) { listQuery.setParameter(any<String>(), any<Any>()) } // only limit and offset
    }

    @Test
    fun `findFiltered with blank perfil filter does not add perfil condition`() {
        every { entityManager.createNativeQuery(any()) } returns listQuery
        every { listQuery.setParameter(any<String>(), any()) } returns listQuery
        every { listQuery.resultList } returns emptyList<Array<Any?>>()

        val filter = AcessoFilter(perfil = "")
        repository.findFiltered(filter, PageRequest.of(0, 10))

        verify(exactly = 2) { listQuery.setParameter(any<String>(), any<Any>()) }
    }
}
