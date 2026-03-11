package br.com.cashflow.usecase.parametro_management.adapter.external.dto

import br.com.cashflow.usecase.parametro.entity.Parametro
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ParametroResponseDtoTest {
    @Test
    fun `toResponse maps STRING tipo and valorTexto`() {
        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val parametro =
            Parametro(
                id = id,
                chave = "CHAVE",
                valorTexto = "texto valor",
                valorInteiro = null,
                valorDecimal = null,
                tipo = "STRING",
                ativo = true,
                creationUserId = "user1",
                createdAt = createdAt,
            )

        val result = parametro.toResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.chave).isEqualTo("CHAVE")
        assertThat(result.valor).isEqualTo("texto valor")
        assertThat(result.tipo).isEqualTo(EnumTipoParametro.TEXTO)
        assertThat(result.ativo).isTrue()
        assertThat(result.creationUserId).isEqualTo("user1")
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
    }

    @Test
    fun `toResponse maps INTEGER tipo and valorInteiro`() {
        val id = UUID.randomUUID()
        val parametro =
            Parametro(
                id = id,
                chave = "K",
                valorTexto = null,
                valorInteiro = 42L,
                valorDecimal = null,
                tipo = "INTEGER",
                ativo = false,
                creationUserId = "u",
            )

        val result = parametro.toResponse()

        assertThat(result.valor).isEqualTo("42")
        assertThat(result.tipo).isEqualTo(EnumTipoParametro.INTEIRO)
        assertThat(result.ativo).isFalse()
    }

    @Test
    fun `toResponse maps DOUBLE tipo and valorDecimal`() {
        val id = UUID.randomUUID()
        val parametro =
            Parametro(
                id = id,
                chave = "K",
                valorTexto = null,
                valorInteiro = null,
                valorDecimal = 3.14,
                tipo = "DOUBLE",
                ativo = true,
                creationUserId = "u",
            )

        val result = parametro.toResponse()

        assertThat(result.valor).isEqualTo("3.14")
        assertThat(result.tipo).isEqualTo(EnumTipoParametro.DECIMAL)
    }

    @Test
    fun `toResponse uses TEXTO when tipo unknown`() {
        val id = UUID.randomUUID()
        val parametro =
            Parametro(
                id = id,
                chave = "K",
                valorTexto = "x",
                tipo = "UNKNOWN",
                ativo = true,
                creationUserId = "u",
            )

        val result = parametro.toResponse()

        assertThat(result.tipo).isEqualTo(EnumTipoParametro.TEXTO)
        assertThat(result.valor).isEqualTo("")
    }

    @Test
    fun `TipoParametro toDbTipo returns STRING DOUBLE INTEGER`() {
        assertThat(EnumTipoParametro.TEXTO.toDbTipo()).isEqualTo("STRING")
        assertThat(EnumTipoParametro.DECIMAL.toDbTipo()).isEqualTo("DOUBLE")
        assertThat(EnumTipoParametro.INTEIRO.toDbTipo()).isEqualTo("INTEGER")
    }

    @Test
    fun `TipoParametro fromDbTipo returns enum for STRING INTEGER DOUBLE`() {
        assertThat(EnumTipoParametro.fromDbTipo("STRING")).isEqualTo(EnumTipoParametro.TEXTO)
        assertThat(EnumTipoParametro.fromDbTipo("string")).isEqualTo(EnumTipoParametro.TEXTO)
        assertThat(EnumTipoParametro.fromDbTipo("INTEGER")).isEqualTo(EnumTipoParametro.INTEIRO)
        assertThat(EnumTipoParametro.fromDbTipo("DOUBLE")).isEqualTo(EnumTipoParametro.DECIMAL)
        assertThat(EnumTipoParametro.fromDbTipo("OTHER")).isNull()
    }
}
