package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

private data class WherePart(
    val clause: String,
    val params: Map<String, Any?>,
)

@Repository
class AcessoRepositoryImpl(
    private val entityManager: EntityManager,
) : AcessoRepositoryCustom {
    override fun findFiltered(
        filter: AcessoFilter?,
        pageable: Pageable,
    ): List<AcessoListItem> {
        val where = buildWhere(filter)
        val sql =
            """
            SELECT a.email, a.nome, a.telefone, a.tipo_acesso, a.ativo, a.data, a.mod_date_time,
                   c.id AS congregacao_id, c.nome AS congregacao_nome
            FROM acesso a
            LEFT JOIN acesso_congregacao ac ON a.email = ac.email
            LEFT JOIN congregacao c ON c.id = ac.congregacao_id
            ${where.clause}
            ORDER BY a.nome ASC
            LIMIT :limit OFFSET :offset
            """.trimIndent()
        val query = entityManager.createNativeQuery(sql)
        where.params.forEach { (k, v) -> if (v != null) query.setParameter(k, v) }
        query.setParameter("limit", pageable.pageSize)
        query.setParameter("offset", pageable.offset)
        @Suppress("UNCHECKED_CAST")
        val rows = query.resultList as List<Array<Any?>>
        return rows.map { row -> mapRowToAcessoListItem(row) }
    }

    override fun countFiltered(filter: AcessoFilter?): Long {
        val where = buildWhere(filter)
        val sql =
            """
            SELECT COUNT(DISTINCT a.email)
            FROM acesso a
            LEFT JOIN acesso_congregacao ac ON a.email = ac.email
            ${where.clause}
            """.trimIndent()
        val query = entityManager.createNativeQuery(sql)
        where.params.forEach { (k, v) -> if (v != null) query.setParameter(k, v) }
        val result = query.singleResult
        return when (result) {
            is Number -> result.toLong()
            else -> 0L
        }
    }

    private fun buildWhere(filter: AcessoFilter?): WherePart {
        val params = mutableMapOf<String, Any?>()
        val conditions = mutableListOf<String>()
        filter?.let { f ->
            f.email?.takeIf { it.isNotBlank() }?.let {
                conditions.add("a.email ILIKE :email")
                params["email"] = "%$it%"
            }
            f.congregacaoId?.let {
                conditions.add("ac.congregacao_id = :congregacaoId")
                params["congregacaoId"] = it
            }
            f.perfil?.takeIf { it.isNotBlank() }?.let {
                conditions.add("a.tipo_acesso = :perfil")
                params["perfil"] = it
            }
            f.ativo?.let {
                conditions.add("a.ativo = :ativo")
                params["ativo"] = it
            }
        }
        val clause = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        return WherePart(clause = clause, params = params)
    }

    private fun mapRowToAcessoListItem(row: Array<Any?>): AcessoListItem {
        fun str(i: Int): String? = (row.getOrNull(i) as? String)

        fun uuid(i: Int): java.util.UUID? {
            val s = row.getOrNull(i)?.toString() ?: return null
            return try {
                java.util.UUID.fromString(s)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        fun instant(i: Int): java.time.Instant? {
            val v = row.getOrNull(i) ?: return null
            return when (v) {
                is java.sql.Timestamp -> v.toInstant()
                is java.time.Instant -> v
                else -> null
            }
        }
        return AcessoListItem(
            email = str(0) ?: "",
            nome = str(1),
            telefone = str(2),
            tipoAcesso = str(3) ?: "",
            ativo = (row.getOrNull(4) as? Boolean) ?: false,
            data = instant(5),
            modDateTime = instant(6),
            congregacaoId = uuid(7),
            congregacaoNome = str(8),
        )
    }
}
