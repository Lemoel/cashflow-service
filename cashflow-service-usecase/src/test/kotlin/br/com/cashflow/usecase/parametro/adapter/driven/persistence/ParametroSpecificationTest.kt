package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParametroSpecificationTest {
    @Test
    fun `fromFilter with null returns conjunction`() {
        val spec = ParametroSpecification.fromFilter(null)
        assertThat(spec).isNotNull
        val root = mockk<Root<Parametro>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.conjunction() } returns mockk()
        assertThat(spec.toPredicate(root, query, cb)).isNotNull
    }

    @Test
    fun `fromFilter with ativo adds equal predicate`() {
        val filter = ParametroFilterModel(ativo = false)
        val spec = ParametroSpecification.fromFilter(filter)
        val root = mockk<Root<Parametro>>()
        val path = mockk<Path<Boolean>>()
        every { root.get<Boolean>("ativo") } returns path
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.equal(path, false) } returns mockk()
        every { cb.and(any<Predicate>()) } returns mockk()
        spec.toPredicate(root, query, cb)
    }
}
