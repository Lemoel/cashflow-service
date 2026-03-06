package br.com.cashflow.usecase.congregation.util

import br.com.cashflow.commons.util.CnpjValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CnpjValidatorTest {
    @Test
    fun `clean returns only digits`() {
        assertThat(CnpjValidator.clean("12.345.678/0001-90")).isEqualTo("12345678000190")
        assertThat(CnpjValidator.clean(null)).isEqualTo("")
        assertThat(CnpjValidator.clean("12345678000190")).isEqualTo("12345678000190")
    }

    @Test
    fun `isValid returns false for null or blank`() {
        assertThat(CnpjValidator.isValid(null)).isFalse()
        assertThat(CnpjValidator.isValid("")).isFalse()
        assertThat(CnpjValidator.isValid("   ")).isFalse()
    }

    @Test
    fun `isValid returns false when not 14 digits`() {
        assertThat(CnpjValidator.isValid("123")).isFalse()
        assertThat(CnpjValidator.isValid("123456780001901")).isFalse()
    }

    @Test
    fun `isValid returns false when all digits equal`() {
        assertThat(CnpjValidator.isValid("11111111111111")).isFalse()
    }

    @Test
    fun `isValid returns true for valid CNPJ`() {
        assertThat(CnpjValidator.isValid("11222333000181")).isTrue()
        assertThat(CnpjValidator.isValid("11.222.333/0001-81")).isTrue()
    }

    @Test
    fun `isValid returns false for invalid check digits`() {
        assertThat(CnpjValidator.isValid("11222333000182")).isFalse()
    }
}
