package br.com.cashflow.usecase.acesso.model

data class AcessoPage(
    val items: List<AcessoListItem>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
