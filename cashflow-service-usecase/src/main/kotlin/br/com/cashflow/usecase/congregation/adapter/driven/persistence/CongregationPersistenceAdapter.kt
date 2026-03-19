package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import br.com.cashflow.usecase.congregation.model.CongregationPageModel
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CongregationPersistenceAdapter(
    private val congregationRepository: CongregationRepository,
) : CongregationOutputPort {
    override fun save(congregation: Congregation): Congregation = congregationRepository.save(congregation)

    override fun findById(id: UUID): Congregation? = congregationRepository.findById(id).orElse(null)

    override fun findAll(
        filter: CongregationFilterModel?,
        page: Int,
        size: Int,
    ): CongregationPageModel {
        val pageable = PageRequest.of(page, size)
        val springPage = congregationRepository.findFiltered(filter, pageable)
        return CongregationPageModel(
            items = springPage.content,
            total = springPage.totalElements,
            page = springPage.number,
            pageSize = springPage.size,
        )
    }

    override fun findAllOrderByNome(): List<Pair<UUID, String>> =
        congregationRepository.findAllProjectedByOrderByNomeAsc().map { it.getId() to it.getNome() }

    override fun findSetoriais(): List<Pair<UUID, String>> =
        congregationRepository.findProjectedBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc().map { it.getId() to it.getNome() }

    override fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean =
        if (excludeId != null) {
            congregationRepository.existsByCnpjAndIdNot(cnpj, excludeId)
        } else {
            congregationRepository.existsByCnpj(cnpj)
        }

    override fun deleteById(id: UUID) {
        congregationRepository.deleteById(id)
    }
}
