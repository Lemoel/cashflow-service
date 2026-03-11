package br.com.cashflow.usecase.lancamento.enum

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TipoEventoEnumTest {
    @Test
    fun `fromCode returns VENDA_OU_PAGAMENTO for 1`() {
        assertThat(TipoEventoEnum.fromCode("1")).isEqualTo(TipoEventoEnum.VENDA_OU_PAGAMENTO)
    }

    @Test
    fun `fromCode returns DESCONHECIDO for null or blank`() {
        assertThat(TipoEventoEnum.fromCode(null)).isEqualTo(TipoEventoEnum.DESCONHECIDO)
        assertThat(TipoEventoEnum.fromCode("")).isEqualTo(TipoEventoEnum.DESCONHECIDO)
    }

    @Test
    fun `fromCode returns enum by name`() {
        assertThat(
            TipoEventoEnum.fromCode("VENDA_OU_PAGAMENTO"),
        ).isEqualTo(TipoEventoEnum.VENDA_OU_PAGAMENTO)
    }

    @Test
    fun `fromCode returns DESCONHECIDO for unknown code`() {
        assertThat(TipoEventoEnum.fromCode("99")).isEqualTo(TipoEventoEnum.DESCONHECIDO)
    }
}
