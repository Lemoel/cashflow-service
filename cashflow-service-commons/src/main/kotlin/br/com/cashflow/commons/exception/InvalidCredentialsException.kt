package br.com.cashflow.commons.exception

import org.springframework.security.core.AuthenticationException

class InvalidCredentialsException(
    message: String = "E-mail ou senha inválidos.",
) : AuthenticationException(message)
