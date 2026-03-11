package br.com.cashflow.usecase.user_authentication.port

import br.com.cashflow.usecase.user_authentication.model.LoginResponseModel
import br.com.cashflow.usecase.user_authentication.model.UsuarioResponseModel

interface AuthInputPort {
    fun login(
        email: String,
        password: String,
    ): LoginResponseModel

    fun refresh(refreshToken: String): LoginResponseModel

    fun getCurrentUser(email: String): UsuarioResponseModel

    fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
    )
}
