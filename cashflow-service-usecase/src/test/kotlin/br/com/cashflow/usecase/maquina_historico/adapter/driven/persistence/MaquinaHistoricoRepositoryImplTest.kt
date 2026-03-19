package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

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

class MaquinaHistoricoRepositoryImplTest {
    private val entityManager: EntityManager = mockk()
    private val query: Query = mockk()
    private lateinit var repository: MaquinaHistoricoRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MaquinaHistoricoRepositoryImpl(entityManager)
    }

    @Test
    fun `findByMaquinaIdOrderByDataInicioDesc returns list from query`() {
        val maquinaId = UUID.randomUUID()
        val id = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val now = Instant.now()
        val row =
            arrayOf<Any?>(
                id.toString(),
                maquinaId.toString(),
                congregacaoId.toString(),
                "Cong",
                null,
                null,
                java.sql.Timestamp.from(now),
                null,
            )
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.resultList } returns listOf(row)

        val result = repository.findByMaquinaIdOrderByDataInicioDesc(maquinaId)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(id)
        assertThat(result[0].maquinaId).isEqualTo(maquinaId)
        assertThat(result[0].congregacaoNome).isEqualTo("Cong")
        assertThat(result[0].dataInicio).isEqualTo(now)
        assertThat(result[0].dataFim).isNull()
        verify(exactly = 1) { entityManager.createNativeQuery(any<String>()) }
        verify(exactly = 1) { query.setParameter("maquinaId", maquinaId) }
    }
}
