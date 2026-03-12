package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.UUID

@Component
class AcessoPersistenceAdapter(
    private val acessoRepository: AcessoRepository,
    private val jdbcTemplate: JdbcTemplate,
) : AcessoOutputPort {
    override fun findByEmail(email: String): Acesso? = acessoRepository.findById(email).orElse(null)

    override fun updatePassword(
        email: String,
        passwordHash: String,
    ) {
        val acesso = acessoRepository.findById(email).orElse(null) ?: return
        acesso.password = passwordHash
        acessoRepository.save(acesso)
    }

    override fun findTenantIdByEmail(email: String): UUID? = acessoRepository.findTenantIdByEmail(email)

    override fun save(acesso: Acesso): Acesso {
        val email = requireNotNull(acesso.email) { "Email não pode ser nulo" }
        return if (!acessoRepository.existsById(email)) {
            try {
                jdbcTemplate.update(
                    """
                    INSERT INTO eventos.acesso (email, password, nome, telefone, ativo, tipo_acesso, data, mod_date_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    email,
                    acesso.password,
                    acesso.nome,
                    acesso.telefone,
                    acesso.ativo,
                    acesso.tipoAcesso,
                    acesso.data?.let { Timestamp.from(it) },
                    acesso.modDateTime?.let { Timestamp.from(it) },
                )
            } catch (error: DuplicateKeyException) {
                throw org.springframework.dao.DataIntegrityViolationException(
                    "Já existe um usuário com o e-mail '$email'.",
                    error,
                )
            }
            acessoRepository.findById(email).orElseThrow {
                IllegalStateException("Falha ao recuperar acesso após inserção")
            }
        } else {
            acessoRepository.save(acesso)
        }
    }

    override fun existsByEmailExcluding(
        emailToCheck: String,
        excludeEmail: String?,
    ): Boolean {
        if (!acessoRepository.existsById(emailToCheck)) return false
        return excludeEmail == null || emailToCheck != excludeEmail
    }

    override fun findAll(
        filter: AcessoFilter?,
        page: Int,
        size: Int,
    ): AcessoPage {
        val pageable = PageRequest.of(page, size)
        val total = acessoRepository.countFiltered(filter)
        val items = acessoRepository.findFiltered(filter, pageable)
        return AcessoPage(
            items = items,
            total = total,
            page = pageable.pageNumber,
            pageSize = pageable.pageSize,
        )
    }

    override fun deleteByEmail(email: String) {
        acessoRepository.deleteById(email)
    }

    override fun setCongregacaoForEmail(
        email: String,
        congregacaoId: UUID,
    ) {
        jdbcTemplate.update(
            "DELETE FROM eventos.acesso_congregacao WHERE email = ?",
            email,
        )
        jdbcTemplate.update(
            "INSERT INTO eventos.acesso_congregacao (email, congregacao_id) VALUES (?, ?)",
            email,
            congregacaoId,
        )
    }

    override fun findListItemByEmail(email: String): AcessoListItem? = acessoRepository.findListItemByEmail(email)
}
