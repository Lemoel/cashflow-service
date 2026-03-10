package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import br.com.cashflow.usecase.maquina_historico.entity.MaquinaHistorico
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItem
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MaquinaHistoricoPersistenceAdapter(
    private val maquinaHistoricoRepository: MaquinaHistoricoRepository,
) : MaquinaHistoricoOutputPort {
    override fun listarPorMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItem> =
        maquinaHistoricoRepository
            .findByMaquinaIdOrderByDataInicioDesc(maquinaId)
            .map { it.toItem() }

    override fun fecharPeriodoAtual(maquinaId: UUID) {
        maquinaHistoricoRepository.fecharPeriodoAtual(maquinaId)
    }

    override fun inserirPeriodo(
        maquinaId: UUID,
        congregacaoId: UUID?,
        departamentoId: UUID?,
    ) {
        val entity =
            MaquinaHistorico(
                id = UUID.randomUUID(),
                maquinaId = maquinaId,
                congregacaoId = congregacaoId,
                departamentoId = departamentoId,
                dataInicio = Instant.now(),
                dataFim = null,
            )
        maquinaHistoricoRepository.save(entity)
    }
}

private fun MaquinaHistoricoItemRow.toItem(): MaquinaHistoricoItem =
    MaquinaHistoricoItem(
        id = id,
        maquinaId = maquinaId,
        congregacaoId = congregacaoId,
        congregacaoNome = congregacaoNome,
        departamentoId = departamentoId,
        departamentoNome = departamentoNome,
        dataInicio = dataInicio,
        dataFim = dataFim,
    )
