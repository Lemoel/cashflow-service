package br.com.cashflow.usecase.tenant_management.adapter.external.dto

import br.com.cashflow.usecase.tenant.entity.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantResponseTest {

    @Test
    fun `toResponse maps all fields correctly`() {
        val id = UUID.randomUUID()
        val createdAt = Instant.parse("2025-01-15T10:00:00Z")
        val updatedAt = Instant.parse("2025-01-16T12:00:00Z")
        val tenant =
            Tenant(
                id = id,
                cnpj = "12345678000190",
                tradeName = "Church A",
                companyName = "Company A",
                street = "Street",
                number = "1",
                complement = "Room 2",
                neighborhood = "Center",
                city = "City",
                state = "SP",
                zipCode = "01234567",
                phone = "11999999999",
                email = "a@b.com",
                active = true,
                creationUserId = "user1",
                modUserId = "user2",
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val result = tenant.toResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.cnpj).isEqualTo("12345678000190")
        assertThat(result.tradeName).isEqualTo("Church A")
        assertThat(result.companyName).isEqualTo("Company A")
        assertThat(result.street).isEqualTo("Street")
        assertThat(result.number).isEqualTo("1")
        assertThat(result.complement).isEqualTo("Room 2")
        assertThat(result.neighborhood).isEqualTo("Center")
        assertThat(result.city).isEqualTo("City")
        assertThat(result.state).isEqualTo("SP")
        assertThat(result.zipCode).isEqualTo("01234567")
        assertThat(result.phone).isEqualTo("11999999999")
        assertThat(result.email).isEqualTo("a@b.com")
        assertThat(result.active).isTrue()
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
        assertThat(result.updatedAt).isEqualTo(updatedAt.toString())
    }

    @Test
    fun `toListOption maps id and tradeName as name`() {
        val id = UUID.randomUUID()
        val tenant = Tenant(id = id, cnpj = "1", tradeName = "Church B", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")

        val result = tenant.toListOption()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.name).isEqualTo("Church B")
    }
}
