package br.com.cashflow.usecase.maquina_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import br.com.cashflow.usecase.maquina_management.port.MaquinaManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale
import java.util.UUID

@Service
class MaquinaManagementService(
    private val maquinaOutputPort: MaquinaOutputPort,
    private val maquinaHistoricoOutputPort: MaquinaHistoricoOutputPort,
    private val congregationOutputPort: CongregationOutputPort,
    private val departmentOutputPort: DepartmentOutputPort,
    private val bankOutputPort: BankOutputPort,
) : MaquinaManagementInputPort {
    @Transactional
    override fun create(request: MaquinaCreateRequestDto): MaquinaComCongregacao {
        validarMaquinaIdObrigatorio(request.maquinaId)
        validarCongregacaoObrigatoria(request.congregacaoId)
        validarBancoObrigatorio(request.bancoId)
        request.departamentoId?.let {
            validarDepartamentoMesmoTenantDaCongregacao(request.congregacaoId, it)
        }
        val numeroSerieNormalizado = request.maquinaId.trim().uppercase(Locale.ROOT)
        if (maquinaOutputPort.existsByNumeroSerieLeitor(numeroSerieNormalizado)) {
            throw ConflictException("Já existe uma máquina com este ID")
        }
        val entity =
            Maquina(
                numeroSerieLeitor = numeroSerieNormalizado,
                congregacaoId = request.congregacaoId,
                bancoId = request.bancoId,
                departamentoId = request.departamentoId,
                ativo = request.ativo,
            )
        val saved = maquinaOutputPort.save(entity)
        maquinaHistoricoOutputPort.inserirPeriodo(
            saved.id!!,
            saved.congregacaoId,
            saved.departamentoId,
        )
        return maquinaOutputPort.findByIdWithDetalhes(saved.id!!)!!
    }

    @Transactional
    override fun update(
        id: UUID,
        request: MaquinaUpdateRequestDto,
    ): MaquinaComCongregacao {
        val existing =
            maquinaOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Máquina não encontrada")
        validarCongregacaoObrigatoria(request.congregacaoId)
        validarBancoObrigatorio(request.bancoId)
        request.departamentoId?.let {
            validarDepartamentoMesmoTenantDaCongregacao(request.congregacaoId, it)
        }
        val congregacaoMudou = existing.congregacaoId != request.congregacaoId
        val departamentoMudou = existing.departamentoId != request.departamentoId
        if (congregacaoMudou || departamentoMudou) {
            maquinaHistoricoOutputPort.fecharPeriodoAtual(id)
        }
        existing.congregacaoId = request.congregacaoId
        existing.bancoId = request.bancoId
        existing.departamentoId = request.departamentoId
        existing.ativo = request.ativo
        maquinaOutputPort.save(existing)
        if (congregacaoMudou || departamentoMudou) {
            maquinaHistoricoOutputPort.inserirPeriodo(
                existing.id!!,
                existing.congregacaoId,
                existing.departamentoId,
            )
        }
        return maquinaOutputPort.findByIdWithDetalhes(id)!!
    }

    override fun findById(id: UUID): MaquinaComCongregacao? = maquinaOutputPort.findByIdWithDetalhes(id)

    override fun search(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaPage =
        maquinaOutputPort.findWithFiltersComDetalhes(
            maquinaId?.trim()?.takeIf { it.isNotBlank() },
            congregacao?.trim()?.takeIf { it.isNotBlank() },
            banco?.trim()?.takeIf { it.isNotBlank() },
            departamentoId,
            page,
            size,
        )

    override fun listForOptions(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerie: String?,
        page: Int,
        size: Int,
    ): MaquinaPage =
        maquinaOutputPort.findParaSelecaoHistorico(
            tenantId,
            congregacaoId,
            numeroSerie?.trim()?.takeIf { it.isNotBlank() },
            page,
            size,
        )

    override fun listHistoricoByMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItemModel> = maquinaHistoricoOutputPort.listarPorMaquinaId(maquinaId)

    @Transactional
    override fun delete(id: UUID) {
        if (maquinaOutputPort.findById(id) == null) {
            throw ResourceNotFoundException("Máquina não encontrada")
        }
        try {
            maquinaOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Erro ao excluir a máquina. Verifique se não há registros dependentes.",
            )
        }
    }

    private fun validarMaquinaIdObrigatorio(maquinaId: String?) {
        if (maquinaId.isNullOrBlank()) {
            throw BusinessException("O ID da máquina é obrigatório")
        }
    }

    private fun validarCongregacaoObrigatoria(congregacaoId: UUID?) {
        if (congregacaoId == null) {
            throw BusinessException("É necessário selecionar uma congregação")
        }
        if (congregationOutputPort.findById(congregacaoId) == null) {
            throw BusinessException("Congregação da máquina não encontrada")
        }
    }

    private fun validarBancoObrigatorio(bancoId: UUID?) {
        if (bancoId == null) {
            throw BusinessException("É necessário selecionar um banco")
        }
        if (bankOutputPort.findById(bancoId) == null) {
            throw BusinessException("Banco não encontrado")
        }
    }

    private fun validarDepartamentoMesmoTenantDaCongregacao(
        congregacaoId: UUID,
        departamentoId: UUID,
    ) {
        val congregacao =
            congregationOutputPort.findById(congregacaoId)
                ?: throw BusinessException("Congregação da máquina não encontrada")
        val departamento =
            departmentOutputPort.findById(departamentoId)
                ?: throw BusinessException("Departamento não encontrado")
        if (departamento.tenantId != congregacao.tenantId) {
            throw BusinessException(
                "O departamento deve pertencer ao mesmo tenant da congregação da máquina",
            )
        }
    }
}
