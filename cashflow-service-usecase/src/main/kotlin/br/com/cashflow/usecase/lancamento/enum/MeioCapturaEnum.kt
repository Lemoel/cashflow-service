package br.com.cashflow.usecase.lancamento.enum

import com.fasterxml.jackson.annotation.JsonCreator

enum class MeioCapturaEnum(
    val code: String,
) {
    CHIP("1"),
    TARJA("2"),
    NAO_PRESENCIAL("3"),
    TEF("4"),
    QR_CODE("5"),
    OUTRO("0"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String?): MeioCapturaEnum =
            when {
                code.isNullOrBlank() -> OUTRO
                else -> entries.find { it.code == code || it.name == code } ?: OUTRO
            }
    }
}
