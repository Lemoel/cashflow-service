package br.com.cashflow.usecase.department_management.adapter.external.dto

import br.com.cashflow.usecase.department.entity.Department

data class DepartmentResponse(
    val id: String,
    val tenantId: String,
    val tenantNome: String?,
    val nome: String,
    val ativo: Boolean,
    val createdAt: String,
    val updatedAt: String?,
)

data class DepartmentListOption(
    val id: String,
    val nome: String,
)

data class DepartmentListResponse(
    val items: List<DepartmentResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

fun Department.toResponse(tenantNome: String? = null): DepartmentResponse =
    DepartmentResponse(
        id = id!!.toString(),
        tenantId = tenantId!!.toString(),
        tenantNome = tenantNome,
        nome = nome,
        ativo = ativo,
        createdAt = createdDate?.toString() ?: "",
        updatedAt = lastModifiedDate?.toString(),
    )

fun Department.toListOption(): DepartmentListOption =
    DepartmentListOption(
        id = id!!.toString(),
        nome = nome,
    )
