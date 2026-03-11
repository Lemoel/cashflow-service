package br.com.cashflow.usecase.lancamento.enum

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MeioCapturaEnumTest {
    @Test
    fun `fromCode returns CHIP for 1`() {
        assertThat(MeioCapturaEnum.fromCode("1")).isEqualTo(MeioCapturaEnum.CHIP)
    }

    @Test
    fun `fromCode returns OUTRO for null or blank`() {
        assertThat(MeioCapturaEnum.fromCode(null)).isEqualTo(MeioCapturaEnum.OUTRO)
        assertThat(MeioCapturaEnum.fromCode("")).isEqualTo(MeioCapturaEnum.OUTRO)
    }

    @Test
    fun `fromCode returns OUTRO for unknown code`() {
        assertThat(MeioCapturaEnum.fromCode("99")).isEqualTo(MeioCapturaEnum.OUTRO)
    }
}
