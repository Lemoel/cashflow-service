package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AcessoRepository :
    JpaRepository<Acesso, String>,
    AcessoRepositoryCustom {
    @Query(
        value =
            """
            SELECT c.tenant_id FROM acesso_congregacao ac
                INNER JOIN congregacao c ON c.id = ac.congregacao_id
            WHERE ac.email = :email
            LIMIT 1
            """,
        nativeQuery = true,
    )
    fun findTenantIdByEmail(
        @Param("email") email: String,
    ): UUID?

    @Query(
        value =
            """
            SELECT a.email AS email, a.nome AS nome, a.telefone AS telefone, a.tipo_acesso AS tipoAcesso, a.ativo AS ativo,
                   a.dti_created_date AS createdDate, a.dti_last_modified_date AS lastModifiedDate,
                   c.id AS congregacaoId, c.nome AS congregacaoNome
            FROM acesso a
            LEFT JOIN acesso_congregacao ac ON a.email = ac.email
            LEFT JOIN congregacao c ON c.id = ac.congregacao_id
            WHERE a.email = :email
            LIMIT 1
            """,
        nativeQuery = true,
    )
    fun findListItemByEmail(
        @Param("email") email: String,
    ): AcessoListItemProjection?
}
