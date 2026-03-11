package br.com.cashflow.usecase.pagbank.encryption

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class PagBankEncryptionService(
    @param:Value("\${pagbank.encryption.key}")
    private val keyBase64: String,
) {
    private val algorithm = "AES/GCM/NoPadding"
    private val gcmTagLength = 128
    private val ivLength = 12

    private val secretKey: SecretKey by lazy {
        val decoded = Base64.getDecoder().decode(keyBase64)
        require(decoded.size == 32) { "Chave AES-256 deve ter 32 bytes (Base64 decoded)" }
        SecretKeySpec(decoded, "AES")
    }

    fun encrypt(plaintext: String): String {
        if (plaintext.isBlank()) return ""

        val iv =
            ByteArray(ivLength).also {
                java.security.SecureRandom().nextBytes(it)
            }

        val cipher = Cipher.getInstance(algorithm)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(gcmTagLength, iv))

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext

        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encoded: String): String {
        if (encoded.isBlank()) return ""
        val combined = Base64.getDecoder().decode(encoded)
        require(combined.size >= ivLength + 16) { "Payload criptografado inválido" }
        val iv = combined.copyOfRange(0, ivLength)
        val ciphertext = combined.copyOfRange(ivLength, combined.size)
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(gcmTagLength, iv))
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
}
