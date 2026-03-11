package br.com.cashflow.usecase.lancamento.service

import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.lancamento.port.LancamentoOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.entity.StatusProcessamentoEnum
import br.com.cashflow.usecase.movimento_api.port.MovimentoApiOutputPort
import br.com.cashflow.usecase.pagbank.client.LancamentoDetalhe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class LancamentoProcessingServiceTest {
    private val lancamentoOutputPort: LancamentoOutputPort = mockk(relaxed = true)
    private val maquinaOutputPort: MaquinaOutputPort = mockk(relaxed = true)
    private val movimentoApiOutputPort: MovimentoApiOutputPort = mockk(relaxed = true)

    private val service =
        LancamentoProcessingService(
            lancamentoOutputPort = lancamentoOutputPort,
            maquinaOutputPort = maquinaOutputPort,
            movimentoApiOutputPort = movimentoApiOutputPort,
        )

    @Test
    fun `processarLancamentos marca como processada quando nao ha detalhes`() {
        val data = LocalDate.of(2025, 1, 10)
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = UUID.randomUUID(), codigo = "290")

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi

        service.processarLancamentos(data, emptyList(), banco)

        verify(exactly = 1) {
            movimentoApiOutputPort.save(match { it.status == StatusProcessamentoEnum.PROCESSADA })
        }
        verify(exactly = 0) { lancamentoOutputPort.insertIgnorandoDuplicata(any()) }
    }

    @Test
    fun `processarLancamentos cria maquina faltante e insere lancamento`() {
        val data = LocalDate.of(2025, 1, 11)
        val bancoId = UUID.randomUUID()
        val maquinaId = UUID.randomUUID()
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = bancoId, codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX123", numeroSerieLeitor = "SERIE123")

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi
        every { maquinaOutputPort.findByNumeroSerieLeitorIn(setOf("SERIE123")) } returns emptyList()
        every { maquinaOutputPort.save(any()) } returns
            Maquina(id = maquinaId, numeroSerieLeitor = "SERIE123", bancoId = bancoId)

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 1) {
            lancamentoOutputPort.insertIgnorandoDuplicata(
                match {
                    it.codigoTransacao == "TX123" && it.maquinaId == maquinaId
                },
            )
        }
        verify(exactly = 1) {
            movimentoApiOutputPort.save(match { it.status == StatusProcessamentoEnum.PROCESSADA })
        }
    }
}
