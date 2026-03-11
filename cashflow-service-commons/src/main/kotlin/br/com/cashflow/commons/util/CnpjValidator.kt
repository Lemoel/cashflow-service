package br.com.cashflow.commons.util

object CnpjValidator {
    private const val CNPJ_DIGITS_LENGTH = 14

    fun isValid(cnpj: String?): Boolean {
        if (cnpj.isNullOrBlank()) return false
        val digits = clean(cnpj)
        if (digits.length != CNPJ_DIGITS_LENGTH) return false
        if (allDigitsEqual(digits)) return false
        val d1 = checkDigit(digits.substring(0, 12), intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2))
        val d2 =
            checkDigit(digits.substring(0, 13), intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2))
        return d1 == digits[12].digitToInt() && d2 == digits[13].digitToInt()
    }

    fun clean(cnpj: String?): String {
        if (cnpj == null) return ""
        return cnpj.filter { it.isDigit() }
    }

    private fun allDigitsEqual(s: String): Boolean {
        if (s.length < 2) return false
        val first = s[0]
        return s.all { it == first }
    }

    private fun checkDigit(
        base: String,
        multipliers: IntArray,
    ): Int {
        var sum = 0
        for (i in base.indices) {
            sum += base[i].digitToInt() * multipliers[i]
        }
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }
}
