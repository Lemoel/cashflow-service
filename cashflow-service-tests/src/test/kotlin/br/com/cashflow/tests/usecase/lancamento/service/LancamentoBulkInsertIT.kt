package br.com.cashflow.tests.usecase.lancamento.service

import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.usecase.lancamento.adapter.driven.persistence.LancamentoRepository
import br.com.cashflow.usecase.lancamento.entity.Lancamento
import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SqlSetUp(value = ["/db/scripts/lancamento_bulk/load.sql"])
@SqlTearDown(value = ["/db/scripts/lancamento_bulk/teardown.sql"])
class LancamentoBulkInsertIT : PostgresqlBaseTest() {
    @Autowired
    private lateinit var lancamentoRepository: LancamentoRepository

    private val maquinaId = UUID.fromString("11111111-2222-3333-4444-555555555501")

    @Test
    fun should_InsertTwoRows_When_BulkWithValidMaquinaId() {
        val list =
            listOf(
                lancamentoBase("IT-BULK-A", maquinaId),
                lancamentoBase("IT-BULK-B", maquinaId),
            )
        lancamentoRepository.batchInsertIgnorandoDuplicatas(list)

        val count =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamento WHERE codigo_transacao LIKE 'IT-BULK-%'",
                Int::class.java,
            )
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun should_NotDuplicate_When_SameConflictKeyInsertedTwice() {
        val row = lancamentoBase("IT-BULK-DUP", maquinaId)
        lancamentoRepository.batchInsertIgnorandoDuplicatas(listOf(row))
        lancamentoRepository.batchInsertIgnorandoDuplicatas(listOf(row))

        val count =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamento WHERE codigo_transacao = 'IT-BULK-DUP'",
                Int::class.java,
            )
        assertThat(count).isEqualTo(1)
    }

    private fun lancamentoBase(
        codigoTransacao: String,
        maquinaIdRef: UUID?,
    ): Lancamento {
        val data = LocalDate.of(2025, 3, 1)
        return Lancamento(
            nsu = "NSU",
            tid = "TID",
            codigoTransacao = codigoTransacao,
            parcela = "1",
            tipoEvento = TipoEventoEnum.VENDA_OU_PAGAMENTO,
            meioCaptura = MeioCapturaEnum.CHIP,
            valorParcela = BigDecimal.ONE,
            meioPagamento = MeioPagamentoEnum.PIX,
            estabelecimento = "EST",
            pagamentoPrazo = "S",
            taxaIntermediacao = BigDecimal.ZERO,
            numeroSerieLeitor = "SERIE_IT_BULK",
            valorTotalTransacao = BigDecimal.TEN,
            dataInicialTransacao = data,
            horaInicialTransacao = "10:00:00",
            dataPrevistaPagamento = data,
            valorLiquidoTransacao = BigDecimal.TEN,
            valorOriginalTransacao = BigDecimal.TEN,
            maquinaId = maquinaIdRef,
        ).apply {
            createdBy = "BOT"
            lastModifiedBy = "BOT"
        }
    }
}
