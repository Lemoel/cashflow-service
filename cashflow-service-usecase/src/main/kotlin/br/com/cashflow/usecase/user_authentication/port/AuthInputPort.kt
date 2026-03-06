package br.com.cashflow.usecase.user_authentication.port

interface AuthInputPort {
    fun login(
        email: String,
        password: String,
    ): LoginResponse

    fun refresh(refreshToken: String): LoginResponse

    fun getCurrentUser(email: String): UsuarioResponse

    fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
    )
}
