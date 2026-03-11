package br.com.cashflow.usecase.pagbank.encryption

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Base64

class PagBankEncryptionServiceTest {
    @Test
    fun `encrypt and decrypt roundtrip`() {
        val keyBytes = ByteArray(32) { it.toByte() }
        val keyBase64 = Base64.getEncoder().encodeToString(keyBytes)
        val service = PagBankEncryptionService(keyBase64)
        val plain = "{\"detalhes\":[],\"pagination\":{}}"
        val encrypted = service.encrypt(plain)
        assertThat(encrypted).isNotBlank().isNotEqualTo(plain)
        val decrypted = service.decrypt(encrypted)
        assertThat(decrypted).isEqualTo(plain)
    }

    @Test
    fun `encrypt empty string returns empty`() {
        val keyBytes = ByteArray(32) { it.toByte() }
        val service = PagBankEncryptionService(Base64.getEncoder().encodeToString(keyBytes))
        assertThat(service.encrypt("")).isEmpty()
    }

    @Test
    fun `decrypt empty string returns empty`() {
        val keyBytes = ByteArray(32) { it.toByte() }
        val service = PagBankEncryptionService(Base64.getEncoder().encodeToString(keyBytes))
        assertThat(service.decrypt("")).isEmpty()
    }
}
