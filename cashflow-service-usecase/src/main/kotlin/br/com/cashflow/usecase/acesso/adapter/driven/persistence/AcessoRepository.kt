package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AcessoRepository :
    CrudRepository<Acesso, String>,
    AcessoRepositoryCustom {
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

    @Query(
        value =
            """
            SELECT a.email, a.nome, a.telefone, a.tipo_acesso, a.ativo, a.data, a.mod_date_time,
                   c.id AS congregacao_id, c.nome AS congregacao_nome
            FROM eventos.acesso a
            LEFT JOIN eventos.acesso_congregacao ac ON a.email = ac.email
            LEFT JOIN eventos.congregacao c ON c.id = ac.congregacao_id
            WHERE a.email = :email
            LIMIT 1
            """,
        rowMapperClass = AcessoListItemRowMapper::class,
    )
    fun findListItemByEmail(
        @Param("email") email: String,
    ): AcessoListItem?
}
