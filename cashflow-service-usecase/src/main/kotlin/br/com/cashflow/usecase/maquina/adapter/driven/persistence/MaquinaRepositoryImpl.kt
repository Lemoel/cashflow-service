package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class MaquinaRepositoryImpl(
    private val entityManager: EntityManager,
) : MaquinaRepositoryCustom {
    override fun findByIdWithDetalhes(id: UUID): MaquinaComCongregacao? {
        val sql =
            """
            SELECT
                m.id,
                m.numero_serie_leitor AS maquina_id,
                m.congregacao_id,
                COALESCE(c.nome, 'Não informada') AS congregacao_nome,
                m.banco_id,
                COALESCE(b.nome, 'Não informado') AS banco_nome,
                m.departamento_id,
                d.nome AS departamento_nome,
                m.ativo,
                m.version,
                m.created_at,
                m.updated_at
            FROM maquina m
            LEFT JOIN congregacao c ON m.congregacao_id = c.id
            LEFT JOIN banco b ON m.banco_id = b.id
            LEFT JOIN departamento d ON m.departamento_id = d.id
            WHERE m.id = :id
            """.trimIndent()
        val query = entityManager.createNativeQuery(sql).setParameter("id", id)

        @Suppress("UNCHECKED_CAST")
        val rows = query.resultList as List<Array<Any?>>
        return rows.firstOrNull()?.let { mapRowToMaquinaComCongregacao(it) }
    }

    override fun findWithFiltersComDetalhes(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any?>()
        if (!maquinaId.isNullOrBlank()) {
            conditions.add("(m.numero_serie_leitor::text ILIKE :maquinaId)")
            params["maquinaId"] = "%$maquinaId%"
        }
        if (!congregacao.isNullOrBlank()) {
            conditions.add("(c.nome ILIKE :congregacao)")
            params["congregacao"] = "%$congregacao%"
        }
        if (!banco.isNullOrBlank()) {
            conditions.add("(b.nome ILIKE :banco)")
            params["banco"] = "%$banco%"
        }
        if (departamentoId != null) {
            conditions.add("m.departamento_id = :departamentoId")
            params["departamentoId"] = departamentoId
        }
        return findMaquinaComCongregacaoPaginated(conditions, params, page, size)
    }

    override fun findParaSelecaoHistorico(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerieLeitor: String?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any?>()
        if (tenantId != null) {
            conditions.add("c.tenant_id = :tenantId")
            params["tenantId"] = tenantId
        }
        if (congregacaoId != null) {
            conditions.add("m.congregacao_id = :congregacaoId")
            params["congregacaoId"] = congregacaoId
        }
        if (!numeroSerieLeitor.isNullOrBlank()) {
            conditions.add("(m.numero_serie_leitor::text ILIKE :numeroSerieLeitor)")
            params["numeroSerieLeitor"] = "%$numeroSerieLeitor%"
        }
        return findMaquinaComCongregacaoPaginated(conditions, params, page, size)
    }

    private fun findMaquinaComCongregacaoPaginated(
        conditions: List<String>,
        params: MutableMap<String, Any?>,
        page: Int,
        size: Int,
    ): MaquinaQueryResult {
        val whereClause =
            if (conditions.isEmpty()) "" else " WHERE " + conditions.joinToString(" AND ")
        val countSql = "SELECT COUNT(*) $MAQUINA_FROM_CLAUSE$whereClause"
        val countQuery = entityManager.createNativeQuery(countSql)
        params.forEach { (k, v) -> if (v != null) countQuery.setParameter(k, v) }
        val total = (countQuery.singleResult as Number).toLong()

        val selectSql =
            """
            SELECT
            $MAQUINA_SELECT_COLUMNS
            $MAQUINA_FROM_CLAUSE$whereClause
            ORDER BY m.numero_serie_leitor
            LIMIT :limit OFFSET :offset
            """.trimIndent()
        val selectQuery = entityManager.createNativeQuery(selectSql)
        params.forEach { (k, v) -> if (v != null) selectQuery.setParameter(k, v) }
        selectQuery.setParameter("limit", size)
        selectQuery.setParameter("offset", page * size)
        @Suppress("UNCHECKED_CAST")
        val rows = selectQuery.resultList as List<Array<Any?>>
        val items = rows.map { mapRowToMaquinaComCongregacao(it) }
        return MaquinaQueryResult(items = items, total = total)
    }

    private fun mapRowToMaquinaComCongregacao(row: Array<Any?>): MaquinaComCongregacao {
        fun uuid(i: Int): UUID? {
            val s = row.getOrNull(i)?.toString() ?: return null
            return try {
                UUID.fromString(s)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        fun instant(i: Int): Instant? {
            val v = row.getOrNull(i) ?: return null
            return when (v) {
                is java.sql.Timestamp -> v.toInstant()
                is Instant -> v
                else -> null
            }
        }
        return MaquinaComCongregacao(
            id = uuid(0)!!,
            maquinaId = row.getOrNull(1)?.toString() ?: "",
            congregacaoId = uuid(2),
            congregacaoNome = row.getOrNull(3)?.toString() ?: "",
            bancoId = uuid(4),
            bancoNome = row.getOrNull(5)?.toString() ?: "",
            departamentoId = uuid(6),
            departamentoNome = row.getOrNull(7)?.toString(),
            ativo = (row.getOrNull(8) as? Boolean) ?: false,
            version = (row.getOrNull(9) as? Number)?.toLong(),
            createdAt = instant(10),
            updatedAt = instant(11),
        )
    }

    companion object {
        private val MAQUINA_FROM_CLAUSE =
            """
            FROM maquina m
            LEFT JOIN congregacao c ON m.congregacao_id = c.id
            LEFT JOIN banco b ON m.banco_id = b.id
            LEFT JOIN departamento d ON m.departamento_id = d.id
            """.trimIndent()

        private val MAQUINA_SELECT_COLUMNS =
            """
            m.id,
            m.numero_serie_leitor AS maquina_id,
            m.congregacao_id,
            COALESCE(c.nome, 'Não informada') AS congregacao_nome,
            m.banco_id,
            COALESCE(b.nome, 'Não informado') AS banco_nome,
            m.departamento_id,
            d.nome AS departamento_nome,
            m.ativo,
            m.version,
            m.created_at,
            m.updated_at
            """.trimIndent()
    }
}
