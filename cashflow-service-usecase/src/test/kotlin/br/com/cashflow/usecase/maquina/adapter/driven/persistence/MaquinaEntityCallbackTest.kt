package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.entity.Maquina
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AuditorAware
import java.time.Instant
import java.util.Optional
import java.util.UUID

class MaquinaEntityCallbackTest {
    private val auditorAware: AuditorAware<String> = mockk()
    private lateinit var callback: MaquinaEntityCallback

    @BeforeEach
    fun setUp() {
        callback = MaquinaEntityCallback(auditorAware)
    }

    @Test
    fun `onBeforeConvert for new entity sets id creationUserId modUserId createdAt updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user1")
        val maquina =
            Maquina(
                numeroSerieLeitor = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )

        val result = callback.onBeforeConvert(maquina)

        assertThat(result).isSameAs(maquina)
        assertThat(result.id).isNotNull()
        assertThat(result.creationUserId).isEqualTo("user1")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.modUserId).isEqualTo("user1")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert for existing entity preserves id and createdAt and sets modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user2")
        val existingId = UUID.randomUUID()
        val existingCreated = Instant.EPOCH
        val maquina =
            Maquina(
                id = existingId,
                numeroSerieLeitor = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
                creationUserId = "creator",
                createdAt = existingCreated,
            )

        val result = callback.onBeforeConvert(maquina)

        assertThat(result).isSameAs(maquina)
        assertThat(result.id).isEqualTo(existingId)
        assertThat(result.creationUserId).isEqualTo("creator")
        assertThat(result.createdAt).isEqualTo(existingCreated)
        assertThat(result.modUserId).isEqualTo("user2")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert when creationUserId is blank uses auditor`() {
        every { auditorAware.currentAuditor } returns Optional.of("auditor1")
        val maquina =
            Maquina(
                numeroSerieLeitor = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
                creationUserId = "",
            )

        val result = callback.onBeforeConvert(maquina)

        assertThat(result.creationUserId).isEqualTo("auditor1")
    }

    @Test
    fun `onBeforeConvert when auditor is empty uses sistema`() {
        every { auditorAware.currentAuditor } returns Optional.empty()
        val maquina =
            Maquina(
                numeroSerieLeitor = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )

        val result = callback.onBeforeConvert(maquina)

        assertThat(result.creationUserId).isEqualTo("sistema")
        assertThat(result.modUserId).isEqualTo("sistema")
    }
}
