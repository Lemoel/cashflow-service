package br.com.cashflow.usecase.user_management.adapter.external.dto

import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.user_management.port.UsuarioCriadoResult
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
                createdDate = createdAt,
                lastModifiedDate = updatedAt,
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
                createdDate = null,
                lastModifiedDate = null,
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

    @Test
    fun `toUsuarioCriadoResponseDto maps all fields including senhaTemporaria`() {
        val congId = UUID.randomUUID()
        val createdAt = Instant.parse("2025-01-15T10:00:00Z")
        val item =
            AcessoListItem(
                email = "novo@test.com",
                nome = "NOVO USUARIO",
                telefone = "11988887777",
                tipoAcesso = "GESTOR_CONGREGACAO",
                ativo = true,
                createdDate = createdAt,
                lastModifiedDate = null,
                congregacaoId = congId,
                congregacaoNome = "Sede",
            )
        val resultado = UsuarioCriadoResult(usuario = item, senhaTemporaria = "abc123xyz456")

        val dto = resultado.toUsuarioCriadoResponseDto()

        assertThat(dto.id).isEqualTo("novo@test.com")
        assertThat(dto.email).isEqualTo("novo@test.com")
        assertThat(dto.nome).isEqualTo("NOVO USUARIO")
        assertThat(dto.telefone).isEqualTo("11988887777")
        assertThat(dto.perfil).isEqualTo("GESTOR_CONGREGACAO")
        assertThat(dto.congregacaoId).isEqualTo(congId.toString())
        assertThat(dto.congregacaoNome).isEqualTo("Sede")
        assertThat(dto.ativo).isTrue()
        assertThat(dto.createdAt).isEqualTo(createdAt.toString())
        assertThat(dto.updatedAt).isNull()
        assertThat(dto.senhaTemporaria).isEqualTo("abc123xyz456")
    }

    @Test
    fun `toUsuarioCriadoResponseDto handles null congregation`() {
        val item =
            AcessoListItem(
                email = "user@test.com",
                nome = null,
                telefone = null,
                tipoAcesso = "ADMIN",
                ativo = true,
                createdDate = null,
                lastModifiedDate = null,
                congregacaoId = null,
                congregacaoNome = null,
            )
        val resultado = UsuarioCriadoResult(usuario = item, senhaTemporaria = "tempPass123")

        val dto = resultado.toUsuarioCriadoResponseDto()

        assertThat(dto.congregacaoId).isNull()
        assertThat(dto.congregacaoNome).isNull()
        assertThat(dto.nome).isEmpty()
        assertThat(dto.senhaTemporaria).isEqualTo("tempPass123")
    }
}
