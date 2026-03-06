package br.com.cashflow.usecase.user_authentication.legacy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LegacyPasswordSupportTest {
    @Test
    fun `looksLikeBcrypt returns true for valid BCrypt 2a prefix`() {
        val hash = "\$2a\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        assertThat(hash.length).isEqualTo(60)
        assertThat(LegacyPasswordSupport.looksLikeBcrypt(hash)).isTrue()
    }

    @Test
    fun `looksLikeBcrypt returns true for valid BCrypt 2b prefix`() {
        val hash = "\$2b\$12\$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtP2o1eR1qK2u"
        assertThat(LegacyPasswordSupport.looksLikeBcrypt(hash)).isTrue()
    }

    @Test
    fun `looksLikeBcrypt returns false for empty string`() {
        assertThat(LegacyPasswordSupport.looksLikeBcrypt("")).isFalse()
    }

    @Test
    fun `looksLikeBcrypt returns false for null`() {
        assertThat(LegacyPasswordSupport.looksLikeBcrypt(null)).isFalse()
    }

    @Test
    fun `looksLikeBcrypt returns false for SHA-256 hex string`() {
        val sha256Hex = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertThat(sha256Hex.length).isEqualTo(64)
        assertThat(LegacyPasswordSupport.looksLikeBcrypt(sha256Hex)).isFalse()
    }

    @Test
    fun `looksLikeBcrypt returns false for short string with 2a prefix`() {
        assertThat(LegacyPasswordSupport.looksLikeBcrypt("\$2a\$12\$short")).isFalse()
    }

    @Test
    fun `matchesSha256Hex returns true when plain password matches stored hex`() {
        val plainPassword = "senha123"
        val storedHex = LegacyPasswordSupport.sha256Hex(plainPassword)
        assertThat(storedHex).isNotNull()
        assertThat(LegacyPasswordSupport.matchesSha256Hex(plainPassword, storedHex!!)).isTrue()
    }

    @Test
    fun `matchesSha256Hex returns false when plain password does not match stored hex`() {
        val storedHex = LegacyPasswordSupport.sha256Hex("senha123")!!
        assertThat(LegacyPasswordSupport.matchesSha256Hex("wrongpassword", storedHex)).isFalse()
    }

    @Test
    fun `matchesSha256Hex returns false for blank stored hash`() {
        assertThat(LegacyPasswordSupport.matchesSha256Hex("any", "")).isFalse()
    }

    @Test
    fun `matchesSha256Hex is compatible with eventos SHA-256 hex format`() {
        val plainPassword = "admin"
        val hexFromEventos = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"
        assertThat(LegacyPasswordSupport.matchesSha256Hex(plainPassword, hexFromEventos)).isTrue()
    }
}
