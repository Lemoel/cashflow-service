package br.com.cashflow.usecase.lancamento.enum

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MeioPagamentoEnumTest {
    @Test
    fun `fromCode returns PIX for 11`() {
        assertThat(MeioPagamentoEnum.fromCode("11")).isEqualTo(MeioPagamentoEnum.PIX)
    }

    @Test
    fun `fromCode returns OUTRO for null or blank`() {
        assertThat(MeioPagamentoEnum.fromCode(null)).isEqualTo(MeioPagamentoEnum.OUTRO)
        assertThat(MeioPagamentoEnum.fromCode("")).isEqualTo(MeioPagamentoEnum.OUTRO)
    }

    @Test
    fun `fromCode returns OUTRO for unknown code`() {
        assertThat(MeioPagamentoEnum.fromCode("999")).isEqualTo(MeioPagamentoEnum.OUTRO)
    }
}
