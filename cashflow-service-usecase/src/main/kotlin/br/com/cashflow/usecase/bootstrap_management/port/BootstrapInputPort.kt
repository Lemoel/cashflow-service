package br.com.cashflow.usecase.bootstrap_management.port

interface BootstrapInputPort {
    fun bootstrap(
        secret: String,
        command: BootstrapCommand,
    ): BootstrapResult
}
