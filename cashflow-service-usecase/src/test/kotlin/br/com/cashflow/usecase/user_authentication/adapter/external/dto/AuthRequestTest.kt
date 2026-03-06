package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthRequestTest {
    @Test
    fun `ChangePasswordRequest holds currentPassword and newPassword`() {
        val request = ChangePasswordRequest(currentPassword = "old123", newPassword = "new456")
        assertThat(request.currentPassword).isEqualTo("old123")
        assertThat(request.newPassword).isEqualTo("new456")
    }

    @Test
    fun `LoginRequest holds email and password`() {
        val request = LoginRequest(email = "user@test.com", password = "senha123")
        assertThat(request.email).isEqualTo("user@test.com")
        assertThat(request.password).isEqualTo("senha123")
    }

    @Test
    fun `RefreshTokenRequest holds refreshToken`() {
        val request = RefreshTokenRequest(refreshToken = "jwt.refresh.token")
        assertThat(request.refreshToken).isEqualTo("jwt.refresh.token")
    }
}
