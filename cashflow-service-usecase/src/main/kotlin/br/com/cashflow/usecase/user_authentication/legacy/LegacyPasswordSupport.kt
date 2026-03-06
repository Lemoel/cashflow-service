package br.com.cashflow.usecase.user_authentication.legacy

import java.security.MessageDigest
import java.util.HexFormat

object LegacyPasswordSupport {
    private const val BCRYPT_PREFIX_2A = "\$2a\$"
    private const val BCRYPT_PREFIX_2B = "\$2b\$"
    private const val BCRYPT_PREFIX_2Y = "\$2y\$"
    private const val BCRYPT_LENGTH = 60
    private const val SHA_256 = "SHA-256"

    fun looksLikeBcrypt(storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false
        val prefixOk =
            storedHash.startsWith(BCRYPT_PREFIX_2A) ||
                storedHash.startsWith(BCRYPT_PREFIX_2B) ||
                storedHash.startsWith(BCRYPT_PREFIX_2Y)
        return prefixOk && storedHash.length == BCRYPT_LENGTH
    }

    fun matchesSha256Hex(
        plainPassword: String,
        storedHexHash: String,
    ): Boolean {
        if (storedHexHash.isBlank()) return false
        val computedHex = sha256Hex(plainPassword) ?: return false
        val computedBytes = hexToBytes(computedHex) ?: return false
        val storedBytes = hexToBytes(storedHexHash) ?: return false
        if (computedBytes.size != storedBytes.size) return false
        return MessageDigest.isEqual(computedBytes, storedBytes)
    }

    internal fun sha256Hex(input: String): String? =
        try {
            val digest = MessageDigest.getInstance(SHA_256)
            val hashedBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            bytesToHex(hashedBytes)
        } catch (_: Exception) {
            null
        }

    private fun bytesToHex(bytes: ByteArray): String = HexFormat.of().formatHex(bytes).lowercase()

    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        return try {
            HexFormat.of().parseHex(hex)
        } catch (_: Exception) {
            null
        }
    }
}
