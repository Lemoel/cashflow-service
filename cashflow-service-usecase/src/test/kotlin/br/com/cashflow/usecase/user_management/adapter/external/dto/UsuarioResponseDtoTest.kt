package br.com.cashflow.usecase.user_management.adapter.external.dto

import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UsuarioResponseDtoTest {
    @Test
    fun `toUsuarioResponseDto maps all fields correctly`() {
        val congId = UUID.randomUUID()
        val createdAt = Instant.parse("2025-01-15T10:00:00Z")
        val updatedAt = Instant.parse("2025-01-16T12:00:00Z")
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = "USER NAME",
                telefone = "11999999999",
                tipoAcesso = "ADMIN",
                ativo = true,
                data = createdAt,
                modDateTime = updatedAt,
                congregacaoId = congId,
                congregacaoNome = "Congregacao A",
            )

        val result = item.toUsuarioResponseDto()

        assertThat(result.id).isEqualTo("user@test.com")
        assertThat(result.nome).isEqualTo("USER NAME")
        assertThat(result.email).isEqualTo("user@test.com")
        assertThat(result.telefone).isEqualTo("11999999999")
        assertThat(result.perfil).isEqualTo("ADMIN")
        assertThat(result.congregacaoId).isEqualTo(congId.toString())
        assertThat(result.congregacaoNome).isEqualTo("Congregacao A")
        assertThat(result.ativo).isTrue()
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
        assertThat(result.updatedAt).isEqualTo(updatedAt.toString())
    }

    @Test
    fun `toUsuarioResponseDto handles null fields`() {
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = null,
                telefone = null,
                tipoAcesso = "USER",
                ativo = false,
                data = null,
                modDateTime = null,
                congregacaoId = null,
                congregacaoNome = null,
            )

        val result = item.toUsuarioResponseDto()

        assertThat(result.id).isEqualTo("user@test.com")
        assertThat(result.nome).isEmpty()
        assertThat(result.telefone).isNull()
        assertThat(result.congregacaoId).isNull()
        assertThat(result.congregacaoNome).isNull()
        assertThat(result.ativo).isFalse()
        assertThat(result.createdAt).isNull()
        assertThat(result.updatedAt).isNull()
    }
}
