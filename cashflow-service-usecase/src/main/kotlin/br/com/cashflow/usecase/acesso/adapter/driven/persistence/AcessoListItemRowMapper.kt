package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

class AcessoListItemRowMapper : RowMapper<AcessoListItem> {
    override fun mapRow(
        rs: ResultSet,
        rowNum: Int,
    ): AcessoListItem =
        AcessoListItem(
            email = rs.getString("email") ?: "",
            nome = rs.getString("nome"),
            telefone = rs.getString("telefone"),
            tipoAcesso = rs.getString("tipo_acesso") ?: "",
            ativo = rs.getBoolean("ativo"),
            data = timestamp(rs, "data"),
            modDateTime = timestamp(rs, "mod_date_time"),
            congregacaoId = uuid(rs, "congregacao_id"),
            congregacaoNome = rs.getString("congregacao_nome"),
        )

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

    private fun timestamp(
        rs: ResultSet,
        column: String,
    ): Instant? {
        val ts = rs.getTimestamp(column) ?: return null
        return ts.toInstant()
    }
}
