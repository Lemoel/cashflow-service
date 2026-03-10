package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AuditorAware
import java.time.Instant
import java.util.Optional
import java.util.UUID

class ParametroEntityCallbackTest {
    private val auditorAware: AuditorAware<String> = mockk()
    private lateinit var callback: ParametroEntityCallback

    @BeforeEach
    fun setUp() {
        callback = ParametroEntityCallback(auditorAware)
    }

    @Test
    fun `onBeforeConvert for new entity sets id creationUserId modUserId createdAt updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user1")
        val parametro = Parametro(chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)

        val result = callback.onBeforeConvert(parametro)

        assertThat(result.id).isNotNull()
        assertThat(result.creationUserId).isEqualTo("user1")
        assertThat(result.modUserId).isEqualTo("user1")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert for existing entity preserves id creationUserId createdAt and sets modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user2")
        val existingId = UUID.randomUUID()
        val existingCreated = Instant.EPOCH
        val parametro =
            Parametro(
                id = existingId,
                chave = "K",
                valorTexto = "v",
                tipo = "STRING",
                ativo = true,
                creationUserId = "creator",
                createdAt = existingCreated,
            )

        val result = callback.onBeforeConvert(parametro)

        assertThat(result.id).isEqualTo(existingId)
        assertThat(result.creationUserId).isEqualTo("creator")
        assertThat(result.createdAt).isEqualTo(existingCreated)
        assertThat(result.modUserId).isEqualTo("user2")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert when no auditor uses sistema`() {
        every { auditorAware.currentAuditor } returns Optional.empty()
        val parametro = Parametro(chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)

        val result = callback.onBeforeConvert(parametro)

        assertThat(result.creationUserId).isEqualTo("sistema")
        assertThat(result.modUserId).isEqualTo("sistema")
    }
}
