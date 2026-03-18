package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CongregationSpecificationTest {
    @Test
    fun `fromFilter with null returns conjunction`() {
        val spec = CongregationSpecification.fromFilter(null)
        assertThat(spec).isNotNull
        val root = mockk<Root<Congregation>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.conjunction() } returns mockk()
        val pred = spec.toPredicate(root, query, cb)
        assertThat(pred).isNotNull
    }

    @Test
    fun `fromFilter with nome adds like predicate for case insensitive partial match`() {
        val filter = CongregationFilterModel(nome = "Cong A")
        val spec = CongregationSpecification.fromFilter(filter)
        val root = mockk<Root<Congregation>>()
        val path = mockk<Path<String>>()
        val upperPath = mockk<Path<String>>()
        every { root.get<String>("nome") } returns path
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.upper(path) } returns upperPath
        every { cb.like(upperPath, "%CONG A%") } returns mockk()
        every { cb.and(any<Predicate>()) } returns mockk()
        spec.toPredicate(root, query, cb)
    }

    @Test
    fun `fromFilter with ativo adds equal predicate`() {
        val filter = CongregationFilterModel(ativo = true)
        val spec = CongregationSpecification.fromFilter(filter)
        val root = mockk<Root<Congregation>>()
        val path = mockk<Path<Boolean>>()
        every { root.get<Boolean>("ativo") } returns path
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.equal(path, true) } returns mockk()
        every { cb.and(any<Predicate>()) } returns mockk()
        spec.toPredicate(root, query, cb)
    }
}
