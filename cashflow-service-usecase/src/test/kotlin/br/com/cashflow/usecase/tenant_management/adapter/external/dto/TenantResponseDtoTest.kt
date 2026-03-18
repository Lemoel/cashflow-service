package br.com.cashflow.usecase.tenant_management.adapter.external.dto

import br.com.cashflow.usecase.tenant.entity.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class TenantResponseDtoTest {
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
                schemaName = "tenant_12345678000190",
            )
        tenant.createdBy = "user1"
        tenant.lastModifiedBy = "user2"
        tenant.createdDate = LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC)
        tenant.lastModifiedDate = LocalDateTime.ofInstant(updatedAt, ZoneOffset.UTC)

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
        assertThat(result.createdAt).isEqualTo(tenant.createdDate.toString())
        assertThat(result.updatedAt).isEqualTo(tenant.lastModifiedDate.toString())
    }

    @Test
    fun `toListOption maps id and tradeName as name`() {
        val id = UUID.randomUUID()
        val tenant =
            Tenant(
                id = id,
                cnpj = "1",
                tradeName = "Church B",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
                schemaName = "tenant_1",
            )

        val result = tenant.toListOption()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.name).isEqualTo("Church B")
    }

    @Test
    fun `TenantCreateRequestDto toEntity normalizes cnpj and uppercases required fields`() {
        val request =
            TenantCreateRequestDto(
                tradeName = "  church crud  ",
                companyName = "  company ltda  ",
                cnpj = "12.345.678/0001-90",
                street = "  street  ",
                number = " 1 ",
                complement = " room ",
                neighborhood = " center ",
                city = " city ",
                state = "sp",
                zipCode = "01234-567",
                phone = " 11999999999 ",
                email = " A@B.COM ",
                active = false,
            )

        val result = request.toEntity()

        assertThat(result.id).isNull()
        assertThat(result.cnpj).isEqualTo("12345678000190")
        assertThat(result.tradeName).isEqualTo("  CHURCH CRUD  ".trim().uppercase())
        assertThat(result.companyName).isEqualTo("  COMPANY LTDA  ".trim().uppercase())
        assertThat(result.street).isEqualTo("  STREET  ".trim().uppercase())
        assertThat(result.number).isEqualTo("1")
        assertThat(result.complement).isEqualTo(" ROOM ".trim().uppercase())
        assertThat(result.neighborhood).isEqualTo(" CENTER ".trim().uppercase())
        assertThat(result.city).isEqualTo(" CITY ".trim().uppercase())
        assertThat(result.state).isEqualTo("SP")
        assertThat(result.zipCode).isEqualTo("01234-567")
        assertThat(result.phone).isEqualTo("11999999999")
        assertThat(result.email).isEqualTo("a@b.com")
        assertThat(result.active).isFalse()
    }

    @Test
    fun `TenantCreateRequestDto toEntity with null optionals`() {
        val request =
            TenantCreateRequestDto(
                tradeName = "Church",
                cnpj = "11111111000191",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )

        val result = request.toEntity()

        assertThat(result.companyName).isNull()
        assertThat(result.complement).isNull()
        assertThat(result.neighborhood).isNull()
        assertThat(result.phone).isNull()
        assertThat(result.email).isNull()
        assertThat(result.active).isTrue()
    }

    @Test
    fun `TenantUpdateRequestDto applyTo updates tenant fields with normalized values`() {
        val tenant =
            Tenant(
                id = UUID.randomUUID(),
                cnpj = "old",
                tradeName = "Old Name",
                companyName = "Old Co",
                street = "Old St",
                number = "0",
                city = "Old City",
                state = "RJ",
                zipCode = "20000000",
                active = true,
                schemaName = "tenant_old",
            )
        val request =
            TenantUpdateRequestDto(
                tradeName = "  new name  ",
                companyName = "  new company  ",
                street = "  new street  ",
                number = " 2 ",
                complement = " sala ",
                neighborhood = " norte ",
                city = " new city ",
                state = "sp",
                zipCode = " 01234-567 ",
                phone = " 11888887777 ",
                email = " NEW@EMAIL.COM ",
                active = false,
            )

        request.applyTo(tenant)

        assertThat(tenant.cnpj).isEqualTo("old")
        assertThat(tenant.tradeName).isEqualTo("NEW NAME")
        assertThat(tenant.companyName).isEqualTo("NEW COMPANY")
        assertThat(tenant.street).isEqualTo("NEW STREET")
        assertThat(tenant.number).isEqualTo("2")
        assertThat(tenant.complement).isEqualTo("SALA")
        assertThat(tenant.neighborhood).isEqualTo("NORTE")
        assertThat(tenant.city).isEqualTo("NEW CITY")
        assertThat(tenant.state).isEqualTo("SP")
        assertThat(tenant.zipCode).isEqualTo("01234-567")
        assertThat(tenant.phone).isEqualTo("11888887777")
        assertThat(tenant.email).isEqualTo("new@email.com")
        assertThat(tenant.active).isFalse()
    }
}
