package br.com.cashflow.usecase.congregation.model

import br.com.cashflow.usecase.congregation.entity.Congregation

data class CongregationPage(
    val items: List<Congregation>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
