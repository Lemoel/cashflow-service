package br.com.cashflow.usecase.department.model

import br.com.cashflow.usecase.department.entity.Department

data class DepartmentPage(
    val items: List<Department>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
