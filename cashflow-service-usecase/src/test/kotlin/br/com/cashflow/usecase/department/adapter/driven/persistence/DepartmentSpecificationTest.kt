package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DepartmentSpecificationTest {
    @Test
    fun `fromFilter with null returns conjunction`() {
        val spec = DepartmentSpecification.fromFilter(null)
        assertThat(spec).isNotNull
        val root = mockk<Root<Department>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        every { cb.conjunction() } returns mockk()
        assertThat(spec.toPredicate(root, query, cb)).isNotNull
    }

    @Test
    fun `fromFilter with nome adds like predicate`() {
        val filter = DepartmentFilter(nome = "Depto")
        val spec = DepartmentSpecification.fromFilter(filter)
        val root = mockk<Root<Department>>()
        val path = mockk<Path<String>>()
        every { root.get<String>("nome") } returns path
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()
        val lowerPath = mockk<Path<String>>()
        every { cb.lower(path) } returns lowerPath
        every { cb.like(lowerPath, "%depto%") } returns mockk()
        every { cb.and(any<Predicate>()) } returns mockk()
        spec.toPredicate(root, query, cb)
    }
}
