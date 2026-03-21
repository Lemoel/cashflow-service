package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MaquinaPersistenceAdapter(
    private val maquinaRepository: MaquinaRepository,
) : MaquinaOutputPort {
    override fun save(maquina: Maquina): Maquina {
        val saved = maquinaRepository.save(maquina)
        maquinaRepository.flush()
        return saved
    }

    override fun saveAll(maquinas: List<Maquina>): List<Maquina> {
        if (maquinas.isEmpty()) return emptyList()
        val saved = maquinaRepository.saveAll(maquinas).toList()
        maquinaRepository.flush()
        return saved
    }

    override fun findById(id: UUID): Maquina? = maquinaRepository.findById(id).orElse(null)

    override fun findByNumeroSerieLeitorIn(numeroSerieLeitor: Collection<String>): List<Maquina> =
        maquinaRepository.findByNumeroSerieLeitorIn(numeroSerieLeitor)

    override fun findByIdWithDetalhes(id: UUID): MaquinaComCongregacao? = maquinaRepository.findByIdWithDetalhes(id)

    override fun existsByNumeroSerieLeitor(numeroSerieLeitor: String): Boolean = maquinaRepository.existsByNumeroSerieLeitor(numeroSerieLeitor)

    override fun deleteById(id: UUID) {
        maquinaRepository.deleteById(id)
    }

    override fun findWithFiltersComDetalhes(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaPage {
        val result =
            maquinaRepository.findWithFiltersComDetalhes(
                maquinaId,
                congregacao,
                banco,
                departamentoId,
                page,
                size,
            )

        return MaquinaPage(
            items = result.items,
            total = result.total,
            page = page,
            pageSize = size,
        )
    }

    override fun findParaSelecaoHistorico(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerieLeitor: String?,
        page: Int,
        size: Int,
    ): MaquinaPage {
        val result =
            maquinaRepository.findParaSelecaoHistorico(
                tenantId,
                congregacaoId,
                numeroSerieLeitor,
                page,
                size,
            )

        return MaquinaPage(
            items = result.items,
            total = result.total,
            page = page,
            pageSize = size,
        )
    }
}
