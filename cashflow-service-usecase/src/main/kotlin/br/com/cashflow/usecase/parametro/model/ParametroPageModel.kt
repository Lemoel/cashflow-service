package br.com.cashflow.usecase.parametro.model

import br.com.cashflow.usecase.parametro.entity.Parametro

data class ParametroPageModel(
    val items: List<Parametro>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
