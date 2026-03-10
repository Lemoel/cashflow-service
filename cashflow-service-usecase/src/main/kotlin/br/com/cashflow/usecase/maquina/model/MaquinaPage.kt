package br.com.cashflow.usecase.maquina.model

data class MaquinaPage(
    val items: List<MaquinaComCongregacao>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
