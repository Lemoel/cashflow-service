package br.com.cashflow.usecase.user_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.acesso.entity.Acesso
import br.com.cashflow.usecase.acesso.entity.PerfilUsuario
import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.acesso.model.AcessoPage
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioCreateRequestDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioUpdateRequestDto
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UserManagementService(
    private val acessoOutputPort: AcessoOutputPort,
    private val congregationOutputPort: CongregationOutputPort,
    private val passwordEncoder: PasswordEncoder,
) : UserManagementInputPort {
    override fun create(request: UsuarioCreateRequestDto): AcessoListItem {
        val email = request.email.trim().lowercase()
        if (acessoOutputPort.existsByEmailExcluding(email, null)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        validateCongregacaoExists(request.congregacaoId)
        validatePerfil(request.perfil)
        val senhaTemporaria =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(12)
        val passwordHash =
            requireNotNull(passwordEncoder.encode(senhaTemporaria)) { "Password encoding failed" }
        val now = Instant.now()
        val acesso =
            Acesso(
                email = email,
                password = passwordHash,
                data = now,
                modDateTime = null,
                nome = request.nome.trim().uppercase(),
                telefone = request.telefone?.trim()?.takeIf { it.isNotBlank() },
                ativo = request.ativo,
                tipoAcesso = request.perfil.trim().uppercase(),
            )
        acessoOutputPort.save(acesso)
        acessoOutputPort.setCongregacaoForEmail(email, request.congregacaoId)
        return requireNotNull(acessoOutputPort.findListItemByEmail(email)) {
            "Usuário criado mas não encontrado"
        }
    }

    @Transactional
    override fun update(
        id: String,
        request: UsuarioUpdateRequestDto,
    ): AcessoListItem {
        val existing =
            acessoOutputPort.findByEmail(id)
                ?: throw ResourceNotFoundException("Usuário não encontrado.")
        val newEmail = request.email.trim().lowercase()
        if (acessoOutputPort.existsByEmailExcluding(newEmail, id)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        validateCongregacaoExists(request.congregacaoId)
        validatePerfil(request.perfil)
        val now = Instant.now()
        if (id == newEmail) {
            existing.nome = request.nome.trim().uppercase()
            existing.telefone = request.telefone?.trim()?.takeIf { it.isNotBlank() }
            existing.tipoAcesso = request.perfil.trim().uppercase()
            existing.ativo = request.ativo
            existing.modDateTime = now
            acessoOutputPort.save(existing)
            acessoOutputPort.setCongregacaoForEmail(newEmail, request.congregacaoId)
        } else {
            val newAcesso =
                Acesso(
                    email = newEmail,
                    password = existing.password,
                    data = existing.data,
                    modDateTime = now,
                    nome = request.nome.trim().uppercase(),
                    telefone = request.telefone?.trim()?.takeIf { it.isNotBlank() },
                    ativo = request.ativo,
                    tipoAcesso = request.perfil.trim().uppercase(),
                )
            acessoOutputPort.save(newAcesso)
            acessoOutputPort.setCongregacaoForEmail(newEmail, request.congregacaoId)
            acessoOutputPort.deleteByEmail(id)
        }
        return requireNotNull(acessoOutputPort.findListItemByEmail(newEmail)) {
            "Usuário atualizado mas não encontrado"
        }
    }

    override fun findById(id: String): AcessoListItem? = acessoOutputPort.findListItemByEmail(id)

    override fun findAll(
        page: Int,
        size: Int,
        email: String?,
        congregacaoId: UUID?,
        perfil: String?,
        ativo: Boolean?,
    ): AcessoPage {
        val filter =
            AcessoFilter(
                email = email?.takeIf { it.isNotBlank() },
                congregacaoId = congregacaoId,
                perfil = perfil?.takeIf { it.isNotBlank() },
                ativo = ativo,
            )
        return acessoOutputPort.findAll(filter, page, size)
    }

    @Transactional
    override fun delete(id: String) {
        acessoOutputPort.findByEmail(id)
            ?: throw ResourceNotFoundException("Usuário não encontrado.")
        try {
            acessoOutputPort.deleteByEmail(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Este usuário possui registros vinculados e não pode ser excluído.",
            )
        }
    }

    override fun isEmailAvailable(
        email: String,
        excludeId: String?,
    ): Boolean {
        val takenByOther = acessoOutputPort.existsByEmailExcluding(email, excludeId)
        return !takenByOther
    }

    private fun validateCongregacaoExists(congregacaoId: UUID) {
        if (congregationOutputPort.findById(congregacaoId) == null) {
            throw BusinessException("Congregação não encontrada.")
        }
    }

    private fun validatePerfil(perfil: String) {
        val value = perfil.trim().uppercase()
        if (!PerfilUsuario.entries.any { it.name == value }) {
            throw BusinessException("Perfil inválido.")
        }
    }
}
