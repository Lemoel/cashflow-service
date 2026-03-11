package br.com.cashflow.usecase.department_management.adapter.external.dto

import br.com.cashflow.usecase.department.entity.Department
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DepartmentResponseTest {
    @Test
    fun `toResponse maps all fields correctly with tenantNome`() {
        val id = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val createdAt = Instant.parse("2025-01-15T10:00:00Z")
        val updatedAt = Instant.parse("2025-01-16T12:00:00Z")
        val department =
            Department(
                id = id,
                tenantId = tenantId,
                nome = "TI",
                ativo = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val result = department.toResponse(tenantNome = "Igreja Central")

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.tenantId).isEqualTo(tenantId.toString())
        assertThat(result.tenantNome).isEqualTo("Igreja Central")
        assertThat(result.nome).isEqualTo("TI")
        assertThat(result.ativo).isTrue()
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
        assertThat(result.updatedAt).isEqualTo(updatedAt.toString())
    }

    @Test
    fun `toResponse uses empty string for null createdAt and updatedAt`() {
        val id = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val department = Department(id = id, tenantId = tenantId, nome = "Vendas", ativo = false)

        val result = department.toResponse(tenantNome = null)

        assertThat(result.tenantNome).isNull()
        assertThat(result.createdAt).isEqualTo("")
        assertThat(result.updatedAt).isNull()
    }

    @Test
    fun `toListOption maps id and nome`() {
        val id = UUID.randomUUID()
        val department =
            Department(id = id, tenantId = UUID.randomUUID(), nome = "Financeiro", ativo = true)

        val result = department.toListOption()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.nome).isEqualTo("Financeiro")
    }

    @Test
    fun `DepartmentResponse data class holds values`() {
        val response =
            DepartmentResponse(
                id = "id1",
                tenantId = "tid1",
                tenantNome = "Tenant",
                nome = "Dept",
                ativo = true,
                createdAt = "2025-01-01",
                updatedAt = "2025-01-02",
            )
        assertThat(response.id).isEqualTo("id1")
        assertThat(response.tenantId).isEqualTo("tid1")
        assertThat(response.tenantNome).isEqualTo("Tenant")
        assertThat(response.nome).isEqualTo("Dept")
        assertThat(response.ativo).isTrue()
        assertThat(response.createdAt).isEqualTo("2025-01-01")
        assertThat(response.updatedAt).isEqualTo("2025-01-02")
    }

    @Test
    fun `DepartmentListOption data class holds values`() {
        val option = DepartmentListOption(id = "opt1", nome = "Opção A")
        assertThat(option.id).isEqualTo("opt1")
        assertThat(option.nome).isEqualTo("Opção A")
    }

    @Test
    fun `DepartmentListResponse data class holds items total page pageSize`() {
        val items = listOf(DepartmentResponse("1", "t1", null, "D1", true, "", null))
        val response = DepartmentListResponse(items = items, total = 1L, page = 0, pageSize = 10)
        assertThat(response.items).hasSize(1)
        assertThat(response.items[0].nome).isEqualTo("D1")
        assertThat(response.total).isEqualTo(1L)
        assertThat(response.page).isEqualTo(0)
        assertThat(response.pageSize).isEqualTo(10)
    }

    @Test
    fun `DepartmentCreateRequest toEntity maps fields and normalizes nome`() {
        val tenantId = UUID.randomUUID()
        val request = DepartmentCreateRequestDto(nome = "  departamento ti  ", ativo = false)

        val result = request.toEntity(tenantId)

        assertThat(result.tenantId).isEqualTo(tenantId)
        assertThat(result.nome).isEqualTo("  DEPARTAMENTO TI  ".trim().uppercase())
        assertThat(result.ativo).isFalse()
        assertThat(result.id).isNull()
    }

    @Test
    fun `DepartmentCreateRequest toEntity with ativo true`() {
        val tenantId = UUID.randomUUID()
        val request = DepartmentCreateRequestDto(nome = "Vendas", ativo = true)

        val result = request.toEntity(tenantId)

        assertThat(result.ativo).isTrue()
        assertThat(result.nome).isEqualTo("VENDAS")
    }

    @Test
    fun `DepartmentCreateRequest toEntity throws when nome is blank`() {
        val tenantId = UUID.randomUUID()
        val request = DepartmentCreateRequestDto(nome = "   ", ativo = true)

        assertThatThrownBy { request.toEntity(tenantId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Nome do departamento é obrigatório")
    }

    @Test
    fun `DepartmentUpdateRequest applyTo updates department nome and ativo`() {
        val department =
            Department(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                nome = "Antigo",
                ativo = true,
            )
        val request = DepartmentUpdateRequestDto(nome = "  novo nome  ", ativo = false)

        request.applyTo(department)

        assertThat(department.nome).isEqualTo("  NOVO NOME  ".trim().uppercase())
        assertThat(department.ativo).isFalse()
    }

    @Test
    fun `DepartmentUpdateRequest applyTo throws when nome is blank`() {
        val department = Department(nome = "X")
        val request = DepartmentUpdateRequestDto(nome = "", ativo = true)

        assertThatThrownBy { request.applyTo(department) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Nome do departamento é obrigatório")
    }

    @Test
    fun `DepartmentCreateRequest data class holds values`() {
        val request = DepartmentCreateRequestDto(nome = "RH", ativo = false)
        assertThat(request.nome).isEqualTo("RH")
        assertThat(request.ativo).isFalse()
        val withDefault = DepartmentCreateRequestDto(nome = "TI")
        assertThat(withDefault.ativo).isTrue()
    }

    @Test
    fun `DepartmentUpdateRequest data class holds values`() {
        val request = DepartmentUpdateRequestDto(nome = "Financeiro", ativo = true)
        assertThat(request.nome).isEqualTo("Financeiro")
        assertThat(request.ativo).isTrue()
    }
}
