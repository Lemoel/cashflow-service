package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantSpecificationTest {
    @Test
    fun `fromFilter with null returns conjunction`() {
        val spec = TenantSpecification.fromFilter(null)
        assertThat(spec).isNotNull
        val root = mockk<Root<Tenant>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.conjunction() } returns mockk()
        assertThat(spec.toPredicate(root, query, cb)).isNotNull
    }

    @Test
    fun `fromFilter with active adds equal predicate`() {
        val filter = TenantFilter(active = false)
        val spec = TenantSpecification.fromFilter(filter)
        val root = mockk<Root<Tenant>>()
        val path = mockk<Path<Boolean>>()
        every { root.get<Boolean>("active") } returns path
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.equal(path, false) } returns mockk()
        every { cb.and(any<Predicate>()) } returns mockk()
        spec.toPredicate(root, query, cb)
    }
}
