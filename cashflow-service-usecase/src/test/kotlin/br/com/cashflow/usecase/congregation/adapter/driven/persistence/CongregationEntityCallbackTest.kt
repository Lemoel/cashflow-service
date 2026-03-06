package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AuditorAware
import java.time.Instant
import java.util.Optional
import java.util.UUID

class CongregationEntityCallbackTest {
    private val auditorAware: AuditorAware<String> = mockk()
    private lateinit var callback: CongregationEntityCallback

    @BeforeEach
    fun setUp() {
        callback = CongregationEntityCallback(auditorAware)
    }

    @Test
    fun `onBeforeConvert for new entity sets id creationUserId createdAt modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user1")
        val congregation = Congregation(id = null, nome = "Cong A", logradouro = "Rua X", bairro = "Centro", numero = "1", cidade = "SP", uf = "SP", cep = "01234567")

        val result = callback.onBeforeConvert(congregation)

        assertThat(result).isSameAs(congregation)
        assertThat(result.id).isNotNull()
        assertThat(result.creationUserId).isEqualTo("user1")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.modUserId).isEqualTo("user1")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert for existing entity does not change id creationUserId createdAt and sets modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user2")
        val existingId = UUID.randomUUID()
        val existingCreated = Instant.EPOCH
        val congregation = Congregation(
            id = existingId,
            nome = "Cong B",
            logradouro = "Rua Y",
            bairro = "Centro",
            numero = "2",
            cidade = "SP",
            uf = "SP",
            cep = "01234567",
            creationUserId = "creator",
            createdAt = existingCreated,
        )

        val result = callback.onBeforeConvert(congregation)

        assertThat(result).isSameAs(congregation)
        assertThat(result.id).isEqualTo(existingId)
        assertThat(result.creationUserId).isEqualTo("creator")
        assertThat(result.createdAt).isEqualTo(existingCreated)
        assertThat(result.modUserId).isEqualTo("user2")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert when auditor is empty uses sistema for creationUserId and modUserId`() {
        every { auditorAware.currentAuditor } returns Optional.empty()
        val congregation = Congregation(id = null, nome = "Cong C", logradouro = "Rua Z", bairro = "Centro", numero = "3", cidade = "SP", uf = "SP", cep = "01234567")

        val result = callback.onBeforeConvert(congregation)

        assertThat(result.id).isNotNull()
        assertThat(result.creationUserId).isEqualTo("sistema")
        assertThat(result.modUserId).isEqualTo("sistema")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
    }
}
