package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AuditorAware
import java.time.Instant
import java.util.Optional
import java.util.UUID

class TenantEntityCallbackTest {
    private val auditorAware: AuditorAware<String> = mockk()
    private lateinit var callback: TenantEntityCallback

    @BeforeEach
    fun setUp() {
        callback = TenantEntityCallback(auditorAware)
    }

    @Test
    fun `onBeforeConvert for new entity sets id creationUserId createdAt modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user1")
        val tenant = Tenant(cnpj = "1", tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")

        val result = callback.onBeforeConvert(tenant)

        assertThat(result).isSameAs(tenant)
        assertThat(result.id).isNotNull()
        assertThat(result.creationUserId).isEqualTo("user1")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.modUserId).isEqualTo("user1")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert for existing entity preserves id creationUserId createdAt and sets modUserId updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user2")
        val existingId = UUID.randomUUID()
        val existingCreated = Instant.EPOCH
        val tenant =
            Tenant(
                id = existingId,
                cnpj = "1",
                tradeName = "T",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
                creationUserId = "creator",
                createdAt = existingCreated,
            )

        val result = callback.onBeforeConvert(tenant)

        assertThat(result).isSameAs(tenant)
        assertThat(result.id).isEqualTo(existingId)
        assertThat(result.creationUserId).isEqualTo("creator")
        assertThat(result.createdAt).isEqualTo(existingCreated)
        assertThat(result.modUserId).isEqualTo("user2")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert when auditor is empty uses system`() {
        every { auditorAware.currentAuditor } returns Optional.empty()
        val tenant = Tenant(cnpj = "1", tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")

        val result = callback.onBeforeConvert(tenant)

        assertThat(result.creationUserId).isEqualTo("system")
        assertThat(result.modUserId).isEqualTo("system")
    }
}
