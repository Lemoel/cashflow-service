package br.com.cashflow.usecase.lancamento.enum

import com.fasterxml.jackson.annotation.JsonCreator

enum class TipoEventoEnum(
    val code: String,
) {
    VENDA_OU_PAGAMENTO("1"),
    AJUSTE_CREDITO("2"),
    AJUSTE_DEBITO("3"),
    TRANSFERENCIA_OUTROS_BANCOS("4"),
    CHARGEBACK("5"),
    CANCELAMENTO("6"),
    SALDO_INICIAL("7"),
    SALDO_FINAL("8"),
    ABERTURA_DISPUTA("9"),
    ENCERRAMENTO_DISPUTA("10"),
    ABERTURA_PRE_CHARGEBACK("11"),
    ENCERRAMENTO_PRE_CHARGEBACK("12"),
    RENDIMENTO_CONTA("16"),
    SAQUE_CANCELADO("18"),
    DEBITO_DIVISAO_PAGAMENTO("19"),
    CREDITO_DIVISAO_PAGAMENTO("20"),
    AQUISICAO_ENVIO_FACIL("21"),
    APORTE_ENVIO_FACIL("22"),
    RECUPERACAO_EMPRESTIMO("23"),
    ARRECADACAO_EMPRESTIMO("24"),
    SAQUE_ESTORNADO("25"),
    DESCONTO_TAXA_CANCELAMENTO("26"),
    DESCONTO_TAXA_CHARGEBACK("27"),
    DEVOLUCAO_TAXA_REVERSAO_CHARGEBACK("28"),
    BLOQUEIO_CUSTODIA("29"),
    DESBLOQUEIO_CUSTODIA("30"),
    PRAZO_ADIADO_ANALISE("31"),
    PRAZO_LIBERADO("32"),
    DESCONHECIDO("00"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String?): TipoEventoEnum =
            when {
                code.isNullOrBlank() -> DESCONHECIDO
                else -> entries.find { it.code == code || it.name == code } ?: DESCONHECIDO
            }
    }
}
