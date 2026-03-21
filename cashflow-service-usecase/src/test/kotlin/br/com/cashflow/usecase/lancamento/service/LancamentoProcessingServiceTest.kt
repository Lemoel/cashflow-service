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
        verify(exactly = 0) { lancamentoOutputPort.batchInsertIgnorandoDuplicatas(any()) }
    }

    @Test
    fun `processarLancamentos cria maquina faltante e insere lancamento em batch`() {
        val data = LocalDate.of(2025, 1, 11)
        val bancoId = UUID.randomUUID()
        val maquinaId = UUID.randomUUID()
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = bancoId, codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX123", numeroSerieLeitor = "SERIE123")
        val maquinaSalva = Maquina(id = maquinaId, numeroSerieLeitor = "SERIE123", bancoId = bancoId)

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi
        every { maquinaOutputPort.findByNumeroSerieLeitorIn(setOf("SERIE123")) } returns emptyList()
        every { maquinaOutputPort.saveAll(any()) } returns listOf(maquinaSalva)

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 1) {
            lancamentoOutputPort.batchInsertIgnorandoDuplicatas(
                match {
                    it.size == 1 && it[0].codigoTransacao == "TX123" && it[0].maquinaId == maquinaId
                },
            )
        }
        verify(exactly = 1) {
            movimentoApiOutputPort.save(match { it.status == StatusProcessamentoEnum.PROCESSADA })
        }
    }

    @Test
    fun `processarLancamentos normaliza serie com trim ao buscar maquinas`() {
        val data = LocalDate.of(2025, 1, 16)
        val bancoId = UUID.randomUUID()
        val maquinaId = UUID.randomUUID()
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = bancoId, codigo = "290")
        val detalhe =
            LancamentoDetalhe(codigoTransacao = "TX_TRIM", numeroSerieLeitor = "  SERIE123  ")
        val maquinaSalva = Maquina(id = maquinaId, numeroSerieLeitor = "SERIE123", bancoId = bancoId)

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi
        every { maquinaOutputPort.findByNumeroSerieLeitorIn(setOf("SERIE123")) } returns emptyList()
        every { maquinaOutputPort.saveAll(any()) } returns listOf(maquinaSalva)

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 1) { maquinaOutputPort.findByNumeroSerieLeitorIn(setOf("SERIE123")) }
        verify(exactly = 1) {
            lancamentoOutputPort.batchInsertIgnorandoDuplicatas(
                match {
                    it.size == 1 && it[0].codigoTransacao == "TX_TRIM" && it[0].maquinaId == maquinaId
                },
            )
        }
    }

    @Test
    fun `processarLancamentos usa maquina existente e nao cria nova`() {
        val data = LocalDate.of(2025, 1, 12)
        val bancoId = UUID.randomUUID()
        val maquinaId = UUID.randomUUID()
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = bancoId, codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX456", numeroSerieLeitor = "SERIE456")
        val maquinaExistente = Maquina(id = maquinaId, numeroSerieLeitor = "SERIE456", bancoId = bancoId)

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi
        every { maquinaOutputPort.findByNumeroSerieLeitorIn(setOf("SERIE456")) } returns listOf(maquinaExistente)

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 0) { maquinaOutputPort.saveAll(any()) }
        verify(exactly = 1) {
            lancamentoOutputPort.batchInsertIgnorandoDuplicatas(
                match {
                    it.size == 1 && it[0].maquinaId == maquinaId
                },
            )
        }
    }

    @Test
    fun `processarLancamentos marca ERRO_PROCESSAMENTO quando ocorre excecao`() {
        val data = LocalDate.of(2025, 1, 13)
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = UUID.randomUUID(), codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX789", numeroSerieLeitor = "SERIE789")

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi
        every { maquinaOutputPort.findByNumeroSerieLeitorIn(any()) } returns emptyList()
        every { maquinaOutputPort.saveAll(any()) } returns emptyList()
        every { lancamentoOutputPort.batchInsertIgnorandoDuplicatas(any()) } throws RuntimeException("DB error")

        org.assertj.core.api.Assertions
            .assertThatThrownBy {
                service.processarLancamentos(data, listOf(detalhe), banco)
            }.isInstanceOf(RuntimeException::class.java)

        verify(exactly = 1) {
            movimentoApiOutputPort.save(match { it.status == StatusProcessamentoEnum.ERRO_PROCESSAMENTO })
        }
    }

    @Test
    fun `processarLancamentos retorna sem processar quando MovimentoApi status nao e RECEBIDO`() {
        val data = LocalDate.of(2025, 1, 14)
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.PROCESSADA)
        val banco = Bank(id = UUID.randomUUID(), codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX999")

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 0) { lancamentoOutputPort.batchInsertIgnorandoDuplicatas(any()) }
        verify(exactly = 0) { movimentoApiOutputPort.save(any()) }
    }

    @Test
    fun `processarLancamentos insere lancamento sem maquina quando detalhe nao tem numeroSerieLeitor`() {
        val data = LocalDate.of(2025, 1, 15)
        val movimentoApi =
            MovimentoApi(dataLeitura = data, pagina = 1, status = StatusProcessamentoEnum.RECEBIDO)
        val banco = Bank(id = UUID.randomUUID(), codigo = "290")
        val detalhe = LancamentoDetalhe(codigoTransacao = "TX000", numeroSerieLeitor = null)

        every { movimentoApiOutputPort.findByDataLeituraAndPagina(data, 1) } returns movimentoApi

        service.processarLancamentos(data, listOf(detalhe), banco)

        verify(exactly = 0) { maquinaOutputPort.findByNumeroSerieLeitorIn(any()) }
        verify(exactly = 1) {
            lancamentoOutputPort.batchInsertIgnorandoDuplicatas(
                match {
                    it.size == 1 && it[0].maquinaId == null && it[0].codigoTransacao == "TX000"
                },
            )
        }
        verify(exactly = 1) {
            movimentoApiOutputPort.save(match { it.status == StatusProcessamentoEnum.PROCESSADA })
        }
    }
}
