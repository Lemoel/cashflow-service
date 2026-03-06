package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AcessoPersistenceAdapter(
    private val acessoRepository: AcessoRepository,
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
}
