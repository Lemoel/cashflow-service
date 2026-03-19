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
import br.com.cashflow.usecase.congregation.entity.Congregation
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
        val cmd = command.sanitized()
        if (acessoOutputPort.existsByEmailExcluding(cmd.email, null)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        val congregation = findCongregationOrThrow(cmd.congregacaoId)
        validatePerfil(cmd.perfil)
        val senhaTemporaria =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(12)
        val passwordHash = checkNotNull(passwordEncoder.encode(senhaTemporaria)) { "Falha ao codificar senha" }
        val acesso =
            Acesso(
                email = cmd.email,
                password = passwordHash,
                nome = cmd.nome,
                telefone = cmd.telefone,
                ativo = cmd.ativo,
                tipoAcesso = cmd.perfil,
            )
        acessoOutputPort.save(acesso)
        acessoOutputPort.setCongregacaoForEmail(cmd.email, cmd.congregacaoId)
        val tenantId = requireNotNull(congregation.tenantId) { "Congregation must have tenantId" }
        acessoOutputPort.insertUserTenantMap(cmd.email, tenantId)
        val usuario =
            requireNotNull(acessoOutputPort.findListItemByEmail(cmd.email)) {
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
        val cmd = command.sanitized()
        if (acessoOutputPort.existsByEmailExcluding(cmd.email, id)) {
            throw ConflictException("Já existe um usuário com este e-mail.")
        }
        findCongregationOrThrow(cmd.congregacaoId)
        validatePerfil(cmd.perfil)
        if (id == cmd.email) {
            existing.nome = cmd.nome
            existing.telefone = cmd.telefone
            existing.tipoAcesso = cmd.perfil
            existing.ativo = cmd.ativo
            acessoOutputPort.save(existing)
            acessoOutputPort.setCongregacaoForEmail(cmd.email, cmd.congregacaoId)
        } else {
            val newAcesso =
                Acesso(
                    email = cmd.email,
                    password = existing.password,
                    nome = cmd.nome,
                    telefone = cmd.telefone,
                    ativo = cmd.ativo,
                    tipoAcesso = cmd.perfil,
                )
            acessoOutputPort.save(newAcesso)
            acessoOutputPort.setCongregacaoForEmail(cmd.email, cmd.congregacaoId)
            acessoOutputPort.deleteByEmail(id)
        }
        return requireNotNull(acessoOutputPort.findListItemByEmail(cmd.email)) {
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
        if (!acessoOutputPort.existsByEmail(id)) {
            throw ResourceNotFoundException("Usuário não encontrado.")
        }
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

    private fun findCongregationOrThrow(congregacaoId: UUID): Congregation =
        congregationOutputPort.findById(congregacaoId)
            ?: throw BusinessException("Congregação não encontrada.")

    private fun validatePerfil(perfil: String) {
        runCatching { PerfilUsuario.valueOf(perfil.trim().uppercase()) }
            .onFailure { throw BusinessException("Perfil inválido.") }
    }
}
