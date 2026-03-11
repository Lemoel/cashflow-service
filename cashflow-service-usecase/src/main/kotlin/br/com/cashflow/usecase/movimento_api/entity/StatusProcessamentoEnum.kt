package br.com.cashflow.usecase.movimento_api.entity

enum class StatusProcessamentoEnum {
    RECEBIDO,
    PROCESSADA,
    ERRO_PAYLOAD,
    ERRO_COMUNICACAO,
    ERRO_PROCESSAMENTO,
    ERRO_INTERNO,
}
