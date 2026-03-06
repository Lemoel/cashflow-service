package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import org.springframework.data.repository.CrudRepository

interface AcessoRepository :
    CrudRepository<Acesso, String>,
    AcessoRepositoryCustom

interface AcessoRepositoryCustom {
    fun findTenantIdByEmail(email: String): java.util.UUID?
}
