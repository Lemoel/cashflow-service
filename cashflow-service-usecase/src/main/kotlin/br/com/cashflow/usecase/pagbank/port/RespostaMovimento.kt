package br.com.cashflow.usecase.pagbank.port

import br.com.cashflow.usecase.pagbank.client.LancamentoDetalhe

sealed class RespostaMovimento {
    data class Sucesso(
        val detalhes: List<LancamentoDetalhe>,
        val payloadCriptografado: String,
        val totalPaginas: Int,
        val totalElementos: Int,
    ) : RespostaMovimento()

    data object NaoValidada : RespostaMovimento()

    data object SemConteudo : RespostaMovimento()

    data class ErroDesserializacao(
        val mensagem: String,
    ) : RespostaMovimento()
}
