package br.com.cashflow.usecase.department_management.adapter.external.dto

import br.com.cashflow.usecase.department.entity.Department

private fun normalizeNome(value: String): String = value.trim().uppercase()

fun DepartmentCreateRequestDto.toEntity(tenantId: java.util.UUID): Department {
    require(nome.isNotBlank()) { "Nome do departamento é obrigatório." }
    return Department(
        tenantId = tenantId,
        nome = normalizeNome(nome.trim()),
        ativo = ativo,
    )
}

fun DepartmentUpdateRequestDto.applyTo(department: Department) {
    require(nome.isNotBlank()) { "Nome do departamento é obrigatório." }
    department.nome = normalizeNome(nome.trim())
    department.ativo = ativo
}
