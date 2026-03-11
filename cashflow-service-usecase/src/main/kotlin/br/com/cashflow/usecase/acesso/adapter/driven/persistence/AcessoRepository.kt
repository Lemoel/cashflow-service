package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AcessoRepository : CrudRepository<Acesso, String> {
    @Query(
        """
        SELECT c.tenant_id FROM eventos.acesso_congregacao ac
        INNER JOIN eventos.congregacao c ON c.id = ac.congregacao_id
        WHERE ac.email = :email
        LIMIT 1
        """,
    )
    fun findTenantIdByEmail(
        @Param("email") email: String,
    ): UUID?
}
