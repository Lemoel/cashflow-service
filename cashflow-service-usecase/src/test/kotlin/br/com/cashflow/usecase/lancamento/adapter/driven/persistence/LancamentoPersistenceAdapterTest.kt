package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class LancamentoPersistenceAdapterTest {
    private val lancamentoRepository: LancamentoRepository = mockk()
    private lateinit var adapter: LancamentoPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = LancamentoPersistenceAdapter(lancamentoRepository)
    }

    @Test
    fun `insertIgnorandoDuplicata delegates to repository`() {
        val lancamento =
            Lancamento(
                parcela = "01",
                tipoEvento = TipoEventoEnum.VENDA_OU_PAGAMENTO,
                meioCaptura = MeioCapturaEnum.CHIP,
                meioPagamento = MeioPagamentoEnum.PIX,
                valorParcela = BigDecimal.TEN,
                estabelecimento = "",
                pagamentoPrazo = "S",
                taxaIntermediacao = BigDecimal.ONE,
                valorTotalTransacao = BigDecimal.TEN,
                dataInicialTransacao = LocalDate.now(),
                horaInicialTransacao = "12:00:00",
                dataPrevistaPagamento = LocalDate.now(),
                valorLiquidoTransacao = BigDecimal.TEN,
                valorOriginalTransacao = BigDecimal.TEN,
                creationUserId = "BOT",
            )
        justRun {
            lancamentoRepository.insertIgnorandoDuplicata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }

        adapter.insertIgnorandoDuplicata(lancamento)

        verify(exactly = 1) {
            lancamentoRepository.insertIgnorandoDuplicata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }
}
