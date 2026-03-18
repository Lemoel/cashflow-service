package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.congregation.adapter.driven.persistence.CongregationRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AcessoPersistenceAdapter(
    private val acessoRepository: AcessoRepository,
    private val congregationRepository: CongregationRepository,
) : AcessoOutputPort {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

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

    override fun save(acesso: Acesso): Acesso = acessoRepository.save(acesso)

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
        val acesso =
            acessoRepository.findById(email).orElse(null)
                ?: return
        val congregation = congregationRepository.getReferenceById(congregacaoId)
        acesso.congregacoes.clear()
        acesso.congregacoes.add(congregation)
        acessoRepository.save(acesso)
    }

    override fun findListItemByEmail(email: String): AcessoListItem? {
        val proj = acessoRepository.findListItemByEmail(email) ?: return null
        return AcessoListItem(
            email = proj.getEmail(),
            nome = proj.getNome(),
            telefone = proj.getTelefone(),
            tipoAcesso = proj.getTipoAcesso(),
            ativo = proj.getAtivo(),
            createdDate = proj.getCreatedDate(),
            lastModifiedDate = proj.getLastModifiedDate(),
            congregacaoId = proj.getCongregacaoId(),
            congregacaoNome = proj.getCongregacaoNome(),
        )
    }

    override fun insertUserTenantMap(
        email: String,
        tenantId: UUID,
    ) {
        entityManager
            .createNativeQuery(
                """
                INSERT INTO core.user_tenant_map (email, tenant_id, created_by_id, dti_created_date, last_modified_by_id, dti_last_modified_date)
                VALUES (:email, :tenantId, :createdBy, CURRENT_TIMESTAMP, :lastModifiedBy, CURRENT_TIMESTAMP)
                """.trimIndent(),
            ).setParameter("email", email)
            .setParameter("tenantId", tenantId)
            .setParameter("createdBy", "system")
            .setParameter("lastModifiedBy", "system")
            .executeUpdate()
    }
}
