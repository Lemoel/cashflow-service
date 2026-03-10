package br.com.cashflow.usecase.maquina_management.adapter.external.dto

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MaquinaResponseTest {
    @Test
    fun `toResponse maps MaquinaComCongregacao to MaquinaResponse`() {
        val id = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val departamentoId = UUID.randomUUID()
        val createdAt = Instant.EPOCH
        val updatedAt = Instant.now()
        val item =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "M001",
                congregacaoId = congregacaoId,
                congregacaoNome = "Cong A",
                bancoId = bancoId,
                bancoNome = "Banco X",
                departamentoId = departamentoId,
                departamentoNome = "Depto 1",
                ativo = true,
                version = 2L,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val result = item.toResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.maquinaId).isEqualTo("M001")
        assertThat(result.congregacaoId).isEqualTo(congregacaoId.toString())
        assertThat(result.congregacaoNome).isEqualTo("Cong A")
        assertThat(result.bancoId).isEqualTo(bancoId.toString())
        assertThat(result.bancoNome).isEqualTo("Banco X")
        assertThat(result.departamentoId).isEqualTo(departamentoId.toString())
        assertThat(result.departamentoNome).isEqualTo("Depto 1")
        assertThat(result.ativo).isTrue()
        assertThat(result.version).isEqualTo(2L)
        assertThat(result.createdAt).isEqualTo(createdAt.toString())
        assertThat(result.updatedAt).isEqualTo(updatedAt.toString())
    }

    @Test
    fun `toResponse uses empty string for null bancoId and null createdAt`() {
        val id = UUID.randomUUID()
        val item =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "M002",
                congregacaoId = null,
                congregacaoNome = "",
                bancoId = null,
                bancoNome = "",
                departamentoId = null,
                departamentoNome = null,
                ativo = false,
                version = null,
                createdAt = null,
                updatedAt = null,
            )

        val result = item.toResponse()

        assertThat(result.bancoId).isEqualTo("")
        assertThat(result.createdAt).isEqualTo("")
        assertThat(result.updatedAt).isNull()
    }

    @Test
    fun `toHistoricoResponse maps MaquinaHistoricoItem to MaquinaHistoricoResponse`() {
        val id = UUID.randomUUID()
        val maquinaId = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val departamentoId = UUID.randomUUID()
        val dataInicio = Instant.EPOCH
        val dataFim = Instant.now()
        val item =
            MaquinaHistoricoItem(
                id = id,
                maquinaId = maquinaId,
                congregacaoId = congregacaoId,
                congregacaoNome = "Cong",
                departamentoId = departamentoId,
                departamentoNome = "Depto",
                dataInicio = dataInicio,
                dataFim = dataFim,
            )

        val result = item.toHistoricoResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.maquinaId).isEqualTo(maquinaId.toString())
        assertThat(result.congregacaoId).isEqualTo(congregacaoId.toString())
        assertThat(result.congregacaoNome).isEqualTo("Cong")
        assertThat(result.departamentoId).isEqualTo(departamentoId.toString())
        assertThat(result.departamentoNome).isEqualTo("Depto")
        assertThat(result.dataInicio).isEqualTo(dataInicio.toString())
        assertThat(result.dataFim).isEqualTo(dataFim.toString())
    }

    @Test
    fun `toOptionResponse maps MaquinaComCongregacao to MaquinaOptionResponse`() {
        val id = UUID.randomUUID()
        val item =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "OPT",
                congregacaoId = null,
                congregacaoNome = "Cong Nome",
                bancoId = null,
                bancoNome = "",
                departamentoId = null,
                departamentoNome = "Depto Nome",
                ativo = true,
                version = null,
                createdAt = null,
                updatedAt = null,
            )

        val result = item.toOptionResponse()

        assertThat(result.id).isEqualTo(id.toString())
        assertThat(result.maquinaId).isEqualTo("OPT")
        assertThat(result.congregacaoNome).isEqualTo("Cong Nome")
        assertThat(result.departamentoNome).isEqualTo("Depto Nome")
    }
}
