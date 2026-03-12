package br.com.cashflow.usecase.congregation_management.adapter.external.dto

import br.com.cashflow.usecase.congregation.entity.Congregation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CongregationResponseTest {
    @Test
    fun `toResponse maps all fields correctly`() {
        val id = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val setorialId = UUID.randomUUID()
        val createdAt = Instant.parse("2025-01-15T10:00:00Z")
        val updatedAt = Instant.parse("2025-01-16T12:00:00Z")
        val congregation =
            Congregation(
                id = id,
                tenantId = tenantId,
                setorialId = setorialId,
                nome = "Cong A",
                cnpj = "12345678000199",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
                email = "a@b.com",
                telefone = "11999999999",
                ativo = true,
                creationUserId = "user1",
                modUserId = "user2",
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val result = congregation.toResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.tenantId).isEqualTo(tenantId.toString())
        assertThat(result.setorialId).isEqualTo(setorialId.toString())
        assertThat(result.nome).isEqualTo("Cong A")
        assertThat(result.cnpj).isEqualTo("12345678000199")
        assertThat(result.logradouro).isEqualTo("Rua X")
        assertThat(result.bairro).isEqualTo("Centro")
        assertThat(result.numero).isEqualTo("1")
        assertThat(result.cidade).isEqualTo("São Paulo")
        assertThat(result.uf).isEqualTo("SP")
        assertThat(result.cep).isEqualTo("01234567")
        assertThat(result.email).isEqualTo("a@b.com")
        assertThat(result.telefone).isEqualTo("11999999999")
        assertThat(result.ativo).isTrue()
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
        assertThat(result.updatedAt).isEqualTo(updatedAt.toString())
    }

    @Test
    fun `toResponse handles null id and tenantId without NPE`() {
        val congregation =
            Congregation(
                id = null,
                tenantId = null,
                nome = "Cong Null",
                logradouro = "",
                bairro = "",
                numero = "",
                cidade = "",
                uf = "",
                cep = "",
            )

        val result = congregation.toResponse()

        assertThat(result.id).isEmpty()
        assertThat(result.tenantId).isEmpty()
        assertThat(result.nome).isEqualTo("Cong Null")
    }

    @Test
    fun `toListOption handles null id without NPE`() {
        val congregation =
            Congregation(
                id = null,
                nome = "Cong No Id",
                logradouro = "",
                bairro = "",
                numero = "",
                cidade = "",
                uf = "",
                cep = "",
            )

        val result = congregation.toListOption()

        assertThat(result.id).isEmpty()
        assertThat(result.nome).isEqualTo("Cong No Id")
    }

    @Test
    fun `toListOption maps id and nome`() {
        val id = UUID.randomUUID()
        val congregation =
            Congregation(
                id = id,
                nome = "Cong B",
                logradouro = "",
                bairro = "",
                numero = "",
                cidade = "",
                uf = "",
                cep = "",
            )

        val result = congregation.toListOption()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.nome).isEqualTo("Cong B")
    }

    @Test
    fun `CongregationCreateRequest toEntity maps fields and normalizes strings`() {
        val tenantId = UUID.randomUUID()
        val setorialId = UUID.randomUUID()
        val request =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                setorialId = setorialId,
                nome = "  cong c  ",
                cnpj = "12.345.678/0001-99",
                logradouro = " Rua Y ",
                bairro = " centro ",
                numero = " 2 ",
                cidade = " sp ",
                uf = "sp",
                cep = " 01234-567 ",
                email = " C@D.COM ",
                telefone = " 11888887777 ",
                ativo = false,
            )

        val result = request.toEntity()

        assertThat(result.tenantId).isEqualTo(tenantId)
        assertThat(result.setorialId).isEqualTo(setorialId)
        assertThat(result.nome).isEqualTo("  CONG C  ".trim().uppercase())
        assertThat(result.cnpj).isNotNull()
        assertThat(result.logradouro).isEqualTo("Rua Y")
        assertThat(result.bairro).isEqualTo("CENTRO")
        assertThat(result.numero).isEqualTo("2")
        assertThat(result.cidade).isEqualTo("SP")
        assertThat(result.uf).isEqualTo("SP")
        assertThat(result.cep).isEqualTo("01234-567")
        assertThat(result.email).isEqualTo("c@d.com")
        assertThat(result.telefone).isEqualTo("11888887777")
        assertThat(result.ativo).isFalse()
    }

    @Test
    fun `CongregationUpdateRequest applyTo updates congregation fields`() {
        val setorialId = UUID.randomUUID()
        val congregation =
            Congregation(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                setorialId = null,
                nome = "Old",
                logradouro = "Old St",
                bairro = "Old B",
                numero = "0",
                cidade = "Old City",
                uf = "RJ",
                cep = "20000000",
                ativo = true,
            )
        val request =
            CongregationUpdateRequestDto(
                setorialId = setorialId,
                nome = "  new name  ",
                cnpj = "11.222.333/0001-81",
                logradouro = " New St ",
                bairro = " new b ",
                numero = " 10 ",
                cidade = " new city ",
                uf = "sp",
                cep = " 01234-567 ",
                email = " new@e.com ",
                telefone = " 11999998888 ",
                ativo = false,
            )

        request.applyTo(congregation)

        assertThat(congregation.setorialId).isEqualTo(setorialId)
        assertThat(congregation.nome).isEqualTo("NEW NAME")
        assertThat(congregation.logradouro).isEqualTo("New St")
        assertThat(congregation.bairro).isEqualTo("NEW B")
        assertThat(congregation.numero).isEqualTo("10")
        assertThat(congregation.cidade).isEqualTo("NEW CITY")
        assertThat(congregation.uf).isEqualTo("SP")
        assertThat(congregation.cep).isEqualTo("01234-567")
        assertThat(congregation.email).isEqualTo("new@e.com")
        assertThat(congregation.telefone).isEqualTo("11999998888")
        assertThat(congregation.ativo).isFalse()
    }
}
