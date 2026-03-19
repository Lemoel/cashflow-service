package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class MaquinaHistoricoRepositoryImpl(
    private val entityManager: EntityManager,
) : MaquinaHistoricoRepositoryCustom {
    override fun findByMaquinaIdOrderByDataInicioDesc(maquinaId: UUID): List<MaquinaHistoricoItemRow> {
        val sql =
            """
            SELECT
                h.id,
                h.maquina_id,
                h.congregacao_id,
                c.nome AS congregacao_nome,
                h.departamento_id,
                d.nome AS departamento_nome,
                h.data_inicio,
                h.data_fim
            FROM maquina_historico h
            LEFT JOIN congregacao c ON h.congregacao_id = c.id
            LEFT JOIN departamento d ON h.departamento_id = d.id
            WHERE h.maquina_id = :maquinaId
            ORDER BY h.data_inicio DESC
            """.trimIndent()
        val query = entityManager.createNativeQuery(sql).setParameter("maquinaId", maquinaId)

        @Suppress("UNCHECKED_CAST")
        val rows = query.resultList as List<Array<Any?>>
        return rows.map { mapRowToItem(it) }
    }

    private fun mapRowToItem(row: Array<Any?>): MaquinaHistoricoItemRow {
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
        return MaquinaHistoricoItemRow(
            id = uuid(0)!!,
            maquinaId = uuid(1)!!,
            congregacaoId = uuid(2),
            congregacaoNome = row.getOrNull(3)?.toString(),
            departamentoId = uuid(4),
            departamentoNome = row.getOrNull(5)?.toString(),
            dataInicio = instant(6)!!,
            dataFim = instant(7),
        )
    }
}
