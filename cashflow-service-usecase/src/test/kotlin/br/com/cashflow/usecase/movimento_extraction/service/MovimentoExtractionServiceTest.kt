package br.com.cashflow.usecase.movimento_extraction.service

import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.lancamento.service.LancamentoProcessingService
import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.port.MovimentoApiOutputPort
import br.com.cashflow.usecase.pagbank.client.LancamentoDetalhe
import br.com.cashflow.usecase.pagbank.config.PagBankApiProperties
import br.com.cashflow.usecase.pagbank.port.PagBankOutputPort
import br.com.cashflow.usecase.pagbank.port.RespostaMovimento
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class MovimentoExtractionServiceTest {
    private val movimentoApiOutputPort: MovimentoApiOutputPort = mockk(relaxed = true)
    private val bankOutputPort: BankOutputPort = mockk()
    private val pagBankOutputPort: PagBankOutputPort = mockk(relaxed = true)
    private val lancamentoProcessingService: LancamentoProcessingService = mockk(relaxed = true)
    private val pagBankApiProperties =
        PagBankApiProperties().apply {
            inicio = "2025-01-01"
            pageSize = 1000
        }
    private val service =
        MovimentoExtractionService(
            movimentoApiOutputPort = movimentoApiOutputPort,
            bankOutputPort = bankOutputPort,
            pagBankOutputPort = pagBankOutputPort,
            lancamentoProcessingService = lancamentoProcessingService,
            pagBankApiProperties = pagBankApiProperties,
        )

    @Test
    fun `extrairDia throws when PagBank not found`() {
        every { bankOutputPort.findByCodigo("290") } returns null

        assertThatThrownBy { service.extrairDia(LocalDate.of(2025, 1, 15)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("PagBank")
    }

    @Test
    fun `extrairTodosDiasPendentes throws when PagBank not found`() {
        every { bankOutputPort.findByCodigo("290") } returns null
        every { movimentoApiOutputPort.findFirstByOrderByDataLeituraDesc() } returns null

        assertThatThrownBy { service.extrairTodosDiasPendentes() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("PagBank")
    }

    @Test
    fun `extrairTodosDiasPendentes nao chama pagbank quando nao ha dias pendentes`() {
        val pagBank =
            Bank(
                id = UUID.randomUUID(),
                codigo = "290",
                enderecoCompleto = "",
                tipoIntegracao = "API",
                ativo = true,
            )
        every { bankOutputPort.findByCodigo("290") } returns pagBank
        val dataReferencia = LocalDate.now(ZoneOffset.UTC).minusDays(1)
        val ultimo =
            MovimentoApi(
                dataLeitura = dataReferencia,
                pagina = 1,
            )
        every { movimentoApiOutputPort.findFirstByOrderByDataLeituraDesc() } returns ultimo

        service.extrairTodosDiasPendentes()

        verify(exactly = 0) { pagBankOutputPort.buscarMovimentos(any(), any()) }
    }

    @Test
    fun `extrairDia ignores response when validado header is not true`() {
        val pagBank =
            Bank(
                id = UUID.randomUUID(),
                codigo = "290",
                enderecoCompleto = "",
                tipoIntegracao = "API",
                ativo = true,
            )
        every { bankOutputPort.findByCodigo("290") } returns pagBank
        every { pagBankOutputPort.buscarMovimentos(any(), any()) } returns
            RespostaMovimento.NaoValidada

        service.extrairDia(LocalDate.of(2025, 1, 15))
    }

    @Test
    fun `extrairTodosDiasPendentes usa pagina inicial apenas para o primeiro dia`() {
        val pagBank =
            Bank(
                id = UUID.randomUUID(),
                codigo = "290",
                enderecoCompleto = "",
                tipoIntegracao = "API",
                ativo = true,
            )
        val hoje = LocalDate.now()
        val primeiroDiaPendente = hoje.minusDays(2)
        val segundoDiaPendente = hoje.minusDays(1)
        val ultimo =
            MovimentoApi(
                dataLeitura = hoje.minusDays(3),
                pagina = 3,
            )

        every { bankOutputPort.findByCodigo("290") } returns pagBank
        every { movimentoApiOutputPort.findFirstByOrderByDataLeituraDesc() } returns ultimo
        every { pagBankOutputPort.buscarMovimentos(any(), any()) } returns
            RespostaMovimento.NaoValidada

        service.extrairTodosDiasPendentes()

        verify(exactly = 1) { pagBankOutputPort.buscarMovimentos(primeiroDiaPendente, 3) }
        verify(exactly = 1) { pagBankOutputPort.buscarMovimentos(segundoDiaPendente, 1) }
    }

    @Test
    fun `extrairDia agrega paginas e delega processamento de lancamentos`() {
        val data = LocalDate.of(2025, 1, 20)
        val pagBank =
            Bank(
                id = UUID.randomUUID(),
                codigo = "290",
                enderecoCompleto = "",
                tipoIntegracao = "API",
                ativo = true,
            )
        val respostaPagina1 =
            RespostaMovimento.Sucesso(
                detalhes = listOf(LancamentoDetalhe(codigoTransacao = "TX1")),
                payloadCriptografado = "payload-1",
                totalPaginas = 2,
                totalElementos = 2,
            )
        val respostaPagina2 =
            RespostaMovimento.Sucesso(
                detalhes = listOf(LancamentoDetalhe(codigoTransacao = "TX2")),
                payloadCriptografado = "payload-2",
                totalPaginas = 2,
                totalElementos = 2,
            )

        every { bankOutputPort.findByCodigo("290") } returns pagBank
        every { pagBankOutputPort.buscarMovimentos(data, 1) } returns respostaPagina1
        every { pagBankOutputPort.buscarMovimentos(data, 2) } returns respostaPagina2

        service.extrairDia(data)

        verify(exactly = 1) {
            lancamentoProcessingService.processarLancamentos(
                data,
                match {
                    it.size ==
                        2
                },
                pagBank,
            )
        }
    }
}
