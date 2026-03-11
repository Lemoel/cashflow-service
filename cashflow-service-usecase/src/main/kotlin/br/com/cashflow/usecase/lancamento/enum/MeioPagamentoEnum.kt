package br.com.cashflow.usecase.lancamento.enum

import com.fasterxml.jackson.annotation.JsonCreator

enum class MeioPagamentoEnum(
    val code: String,
) {
    OPERACAO_CONTA_ADQUIRENCIA("0"),
    DEBITO_ONLINE("1"),
    BOLETO("2"),
    CARTAO_CREDITO("3"),
    SALDO("4"),
    DEPOSITO_CONTA("7"),
    CARTAO_DEBITO("8"),
    DEBITO_AUTOMATICO("9"),
    VOUCHER("10"),
    PIX("11"),
    CARNE_CREDITO("12"),
    CARNE_DEBITO("13"),
    CARTAO_CREDITO_PRE_PAGO("14"),
    CARTAO_DEBITO_PRE_PAGO("15"),
    CARNE_CREDITO_PRE_PAGO("16"),
    CARNE_DEBITO_PRE_PAGO("17"),
    OUTRO("99"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String?): MeioPagamentoEnum =
            when {
                code.isNullOrBlank() -> OUTRO
                else -> entries.find { it.code == code || it.name == code } ?: OUTRO
            }
    }
}
