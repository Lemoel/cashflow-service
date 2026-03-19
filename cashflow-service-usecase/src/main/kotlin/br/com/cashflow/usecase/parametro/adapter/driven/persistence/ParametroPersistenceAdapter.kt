package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro.model.ParametroPageModel
import br.com.cashflow.usecase.parametro.port.ParametroOutputPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ParametroPersistenceAdapter(
    private val parametroRepository: ParametroRepository,
) : ParametroOutputPort {
    override fun save(parametro: Parametro): Parametro = parametroRepository.save(parametro)

    override fun findById(id: UUID): Parametro? = parametroRepository.findById(id).orElse(null)

    override fun findWithFilters(
        filter: ParametroFilterModel?,
        page: Int,
        size: Int,
    ): ParametroPageModel {
        val pageable = PageRequest.of(page, size)
        val springPage = parametroRepository.findWithFilters(filter, pageable)
        return ParametroPageModel(
            items = springPage.content,
            total = springPage.totalElements,
            page = springPage.number,
            pageSize = springPage.size,
        )
    }

    override fun findAllChaveOrderByChave(): List<String> = parametroRepository.findAllChaveOrderByChaveAsc()

    override fun existsByChave(chave: String): Boolean = parametroRepository.existsByChave(chave)

    override fun existsByChaveExcludingId(
        chave: String,
        excludeId: UUID?,
    ): Boolean =
        if (excludeId != null) {
            parametroRepository.existsByChaveAndIdNot(chave, excludeId)
        } else {
            parametroRepository.existsByChave(chave)
        }

    override fun existsById(id: UUID): Boolean = parametroRepository.existsById(id)

    override fun deleteById(id: UUID) {
        parametroRepository.deleteById(id)
    }
}
