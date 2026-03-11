package br.com.cashflow.usecase.movimento_api.adapter.driven.persistence

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.port.MovimentoApiOutputPort
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class MovimentoApiPersistenceAdapter(
    private val movimentoApiRepository: MovimentoApiRepository,
) : MovimentoApiOutputPort {
    override fun save(movimento: MovimentoApi): MovimentoApi = movimentoApiRepository.save(movimento)

    override fun findFirstByOrderByDataLeituraDesc(): MovimentoApi? = movimentoApiRepository.findFirstByOrderByDataLeituraDesc()

    override fun findByDataLeituraAndPagina(
        dataLeitura: LocalDate,
        pagina: Int,
    ): MovimentoApi? = movimentoApiRepository.findByDataLeituraAndPagina(dataLeitura, pagina)
}
