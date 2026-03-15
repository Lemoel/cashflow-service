package br.com.cashflow.usecase.bootstrap_management.adapter.external.dto

import br.com.cashflow.usecase.bootstrap_management.port.BootstrapResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class BootstrapResponseDtoTest {
    @Test
    fun `toResponseDto maps all fields and builds links with baseUrl`() {
        val tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val congregationId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val result =
            BootstrapResult(
                tenantId = tenantId,
                tenantSchemaName = "tenant_12345678000190",
                adminEmail = "admin@example.com",
                temporaryPassword = "temp-secret",
                congregationId = congregationId,
            )
        val baseUrl = "http://localhost:8081"

        val dto = result.toResponseDto(baseUrl)

        assertThat(dto.tenantId).isEqualTo(tenantId.toString())
        assertThat(dto.tenantSchemaName).isEqualTo("tenant_12345678000190")
        assertThat(dto.adminEmail).isEqualTo("admin@example.com")
        assertThat(dto.temporaryPassword).isEqualTo("temp-secret")
        assertThat(dto.congregationId).isEqualTo(congregationId.toString())
        assertThat(dto._links).containsKey("self")
        assertThat(dto._links).containsKey("tenant")
        assertThat(dto._links).containsKey("login")
        assertThat(dto._links["self"]!!.href).isEqualTo("http://localhost:8081/api/v1/tenants/$tenantId")
        assertThat(dto._links["tenant"]!!.href).isEqualTo("http://localhost:8081/api/v1/tenants/$tenantId")
        assertThat(dto._links["login"]!!.href).isEqualTo("http://localhost:8081/api/v1/auth/login")
        assertThat(dto._links["login"]!!.method).isEqualTo("POST")
    }
}
