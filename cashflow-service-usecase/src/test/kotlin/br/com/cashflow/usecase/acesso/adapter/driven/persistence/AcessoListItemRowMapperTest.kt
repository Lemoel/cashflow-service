package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

class AcessoListItemRowMapperTest {
    private val mapper = AcessoListItemRowMapper()

    @Test
    fun `mapRow maps all fields correctly`() {
        val congId = UUID.randomUUID()
        val dataInstant = Instant.parse("2025-06-01T10:00:00Z")
        val modInstant = Instant.parse("2025-06-02T12:00:00Z")
        val rs =
            mockk<ResultSet> {
                every { getString("email") } returns "user@test.com"
                every { getString("nome") } returns "USER NAME"
                every { getString("telefone") } returns "11999999999"
                every { getString("tipo_acesso") } returns "ADMIN"
                every { getBoolean("ativo") } returns true
                every { getTimestamp("data") } returns Timestamp.from(dataInstant)
                every { getTimestamp("mod_date_time") } returns Timestamp.from(modInstant)
                every { getString("congregacao_id") } returns congId.toString()
                every { getString("congregacao_nome") } returns "Congregacao A"
            }

        val result = mapper.mapRow(rs, 0)

        assertThat(result.email).isEqualTo("user@test.com")
        assertThat(result.nome).isEqualTo("USER NAME")
        assertThat(result.telefone).isEqualTo("11999999999")
        assertThat(result.tipoAcesso).isEqualTo("ADMIN")
        assertThat(result.ativo).isTrue()
        assertThat(result.data).isEqualTo(dataInstant)
        assertThat(result.modDateTime).isEqualTo(modInstant)
        assertThat(result.congregacaoId).isEqualTo(congId)
        assertThat(result.congregacaoNome).isEqualTo("Congregacao A")
    }

    @Test
    fun `mapRow handles null values`() {
        val rs =
            mockk<ResultSet> {
                every { getString("email") } returns null
                every { getString("nome") } returns null
                every { getString("telefone") } returns null
                every { getString("tipo_acesso") } returns null
                every { getBoolean("ativo") } returns false
                every { getTimestamp("data") } returns null
                every { getTimestamp("mod_date_time") } returns null
                every { getString("congregacao_id") } returns null
                every { getString("congregacao_nome") } returns null
            }

        val result = mapper.mapRow(rs, 0)

        assertThat(result.email).isEmpty()
        assertThat(result.nome).isNull()
        assertThat(result.telefone).isNull()
        assertThat(result.tipoAcesso).isEmpty()
        assertThat(result.ativo).isFalse()
        assertThat(result.data).isNull()
        assertThat(result.modDateTime).isNull()
        assertThat(result.congregacaoId).isNull()
        assertThat(result.congregacaoNome).isNull()
    }

    @Test
    fun `mapRow handles invalid UUID gracefully`() {
        val rs =
            mockk<ResultSet> {
                every { getString("email") } returns "user@test.com"
                every { getString("nome") } returns "USER"
                every { getString("telefone") } returns null
                every { getString("tipo_acesso") } returns "USER"
                every { getBoolean("ativo") } returns true
                every { getTimestamp("data") } returns null
                every { getTimestamp("mod_date_time") } returns null
                every { getString("congregacao_id") } returns "invalid-uuid"
                every { getString("congregacao_nome") } returns null
            }

        val result = mapper.mapRow(rs, 0)

        assertThat(result.congregacaoId).isNull()
    }
}
