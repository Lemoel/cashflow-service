package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AuditorAware
import java.time.Instant
import java.util.Optional
import java.util.UUID

class DepartmentEntityCallbackTest {
    private val auditorAware: AuditorAware<String> = mockk()
    private lateinit var callback: DepartmentEntityCallback

    @BeforeEach
    fun setUp() {
        callback = DepartmentEntityCallback(auditorAware)
    }

    @Test
    fun `onBeforeConvert for new entity sets id creationUserId modUserId createdAt updatedAt`() {
        every { auditorAware.currentAuditor } returns Optional.of("user1")
        val department = Department(tenantId = UUID.randomUUID(), nome = "TI", ativo = true)

        val result = callback.onBeforeConvert(department)

        assertThat(result).isSameAs(department)
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
        val department =
            Department(
                id = existingId,
                tenantId = UUID.randomUUID(),
                nome = "Vendas",
                creationUserId = "creator",
                createdAt = existingCreated,
            )

        val result = callback.onBeforeConvert(department)

        assertThat(result).isSameAs(department)
        assertThat(result.id).isEqualTo(existingId)
        assertThat(result.creationUserId).isEqualTo("creator")
        assertThat(result.createdAt).isEqualTo(existingCreated)
        assertThat(result.modUserId).isEqualTo("user2")
        assertThat(result.updatedAt).isNotNull()
    }

    @Test
    fun `onBeforeConvert when creationUserId is blank uses auditor`() {
        every { auditorAware.currentAuditor } returns Optional.of("auditor1")
        val department = Department(tenantId = UUID.randomUUID(), nome = "RH", creationUserId = "")

        val result = callback.onBeforeConvert(department)

        assertThat(result.creationUserId).isEqualTo("auditor1")
    }

    @Test
    fun `onBeforeConvert when auditor is empty uses sistema`() {
        every { auditorAware.currentAuditor } returns Optional.empty()
        val department = Department(tenantId = UUID.randomUUID(), nome = "Dept")

        val result = callback.onBeforeConvert(department)

        assertThat(result.creationUserId).isEqualTo("sistema")
        assertThat(result.modUserId).isEqualTo("sistema")
    }
}
