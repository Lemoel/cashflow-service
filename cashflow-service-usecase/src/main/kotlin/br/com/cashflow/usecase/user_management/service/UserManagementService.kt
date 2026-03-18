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
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import br.com.cashflow.usecase.user_management.port.UsuarioCommand
import br.com.cashflow.usecase.user_management.port.UsuarioCriadoResult
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserManagementService(
    private val acessoOutputPort: AcessoOutputPort,
    private val congregationOutputPort: CongregationOutputPort,
    private val passwordEncoder: PasswordEncoder,
) : UserManagementInputPort {
    @Transactional
    override fun create(command: UsuarioCommand): UsuarioCriadoResult {
        val email = command.email.trim().lowercase()
        if (acessoOutputPort.existsByEmailExcluding(email, null)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        validateCongregacaoExists(command.congregacaoId)
        validatePerfil(command.perfil)
        val senhaTemporaria =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(12)
        val passwordHash = checkNotNull(passwordEncoder.encode(senhaTemporaria)) { "Falha ao codificar senha" }
        val acesso =
            Acesso(
                email = email,
                password = passwordHash,
                nome = command.nome.trim().uppercase(),
                telefone = command.telefone?.trim()?.takeIf { it.isNotBlank() },
                ativo = command.ativo,
                tipoAcesso = command.perfil.trim().uppercase(),
            )
        acessoOutputPort.save(acesso)
        acessoOutputPort.setCongregacaoForEmail(email, command.congregacaoId)
        val congregation = requireNotNull(congregationOutputPort.findById(command.congregacaoId))
        val tenantId = requireNotNull(congregation.tenantId) { "Congregation must have tenantId" }
        acessoOutputPort.insertUserTenantMap(email, tenantId)
        val usuario =
            requireNotNull(acessoOutputPort.findListItemByEmail(email)) {
                "Usuário criado mas não encontrado"
            }
        return UsuarioCriadoResult(usuario = usuario, senhaTemporaria = senhaTemporaria)
    }

    @Transactional
    override fun update(
        id: String,
        command: UsuarioCommand,
    ): AcessoListItem {
        val existing =
            acessoOutputPort.findByEmail(id)
                ?: throw ResourceNotFoundException("Usuário não encontrado.")
        val newEmail = command.email.trim().lowercase()
        if (acessoOutputPort.existsByEmailExcluding(newEmail, id)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        validateCongregacaoExists(command.congregacaoId)
        validatePerfil(command.perfil)
        if (id == newEmail) {
            existing.nome = command.nome.trim().uppercase()
            existing.telefone = command.telefone?.trim()?.takeIf { it.isNotBlank() }
            existing.tipoAcesso = command.perfil.trim().uppercase()
            existing.ativo = command.ativo
            acessoOutputPort.save(existing)
            acessoOutputPort.setCongregacaoForEmail(newEmail, command.congregacaoId)
        } else {
            val newAcesso =
                Acesso(
                    email = newEmail,
                    password = existing.password,
                    nome = command.nome.trim().uppercase(),
                    telefone = command.telefone?.trim()?.takeIf { it.isNotBlank() },
                    ativo = command.ativo,
                    tipoAcesso = command.perfil.trim().uppercase(),
                )
            acessoOutputPort.save(newAcesso)
            acessoOutputPort.setCongregacaoForEmail(newEmail, command.congregacaoId)
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
    ): Boolean = !acessoOutputPort.existsByEmailExcluding(email, excludeId)

    private fun validateCongregacaoExists(congregacaoId: UUID) {
        if (congregationOutputPort.findById(congregacaoId) == null) {
            throw BusinessException("Congregação não encontrada.")
        }
    }

    private fun validatePerfil(perfil: String) {
        runCatching { PerfilUsuario.valueOf(perfil.trim().uppercase()) }
            .onFailure { throw BusinessException("Perfil inválido.") }
    }
}
