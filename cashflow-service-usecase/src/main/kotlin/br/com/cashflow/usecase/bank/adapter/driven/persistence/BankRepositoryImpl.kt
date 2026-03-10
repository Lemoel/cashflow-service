package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

@Repository
class BankRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : BankRepository {
    override fun findById(id: UUID): Bank? {
        val list =
            jdbcTemplate.query(
                "SELECT * FROM eventos.banco WHERE id = ?",
                PreparedStatementSetter { ps -> ps.setObject(1, id, Types.OTHER) },
                BANK_ROW_MAPPER,
            )
        return list.firstOrNull()
    }

    override fun findAllByOrderByNomeAsc(): List<Bank> = jdbcTemplate.query("SELECT * FROM eventos.banco ORDER BY nome ASC", BANK_ROW_MAPPER)

    companion object {
        private val BANK_ROW_MAPPER =
            RowMapper<Bank> { rs: ResultSet, _: Int ->
                Bank(
                    id = uuid(rs, "id"),
                    nome = rs.getString("nome"),
                    codigo = rs.getString("codigo") ?: "",
                    enderecoCompleto = rs.getString("endereco_completo") ?: "",
                    tipoIntegracao = rs.getString("tipo_integracao") ?: "",
                    ativo = rs.getBoolean("ativo"),
                )
            }

        private fun uuid(
            rs: ResultSet,
            column: String,
        ): UUID? {
            val s = rs.getString(column) ?: return null
            return try {
                UUID.fromString(s)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
