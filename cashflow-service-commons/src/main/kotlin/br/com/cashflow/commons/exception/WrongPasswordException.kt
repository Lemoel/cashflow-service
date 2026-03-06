package br.com.cashflow.commons.exception

class WrongPasswordException(
    message: String = "A senha atual está incorreta.",
) : RuntimeException(message)
