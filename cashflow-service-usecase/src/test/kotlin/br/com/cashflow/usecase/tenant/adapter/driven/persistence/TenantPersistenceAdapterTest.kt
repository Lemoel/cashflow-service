package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.adapter.driven.persistence.TenantListProjection
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantIdName
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class TenantPersistenceAdapterTest {
    private val tenantRepository: TenantRepository = mockk()
    private lateinit var adapter: TenantPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = TenantPersistenceAdapter(tenantRepository)
    }

    @Test
    fun `save delegates to repository and returns saved tenant`() {
        val tenant =
            Tenant(
                id = null,
                cnpj = "1",
                tradeName = "A",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        val saved =
            Tenant(
                id = UUID.randomUUID(),
                cnpj = "1",
                tradeName = "A",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantRepository.save(tenant) } returns saved

        val result = adapter.save(tenant)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { tenantRepository.save(tenant) }
    }

    @Test
    fun `findById delegates to repository and returns entity when found`() {
        val id = UUID.randomUUID()
        val tenant =
            Tenant(
                id = id,
                cnpj = "1",
                tradeName = "A",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantRepository.findById(id) } returns Optional.of(tenant)

        val result = adapter.findById(id)

        assertThat(result).isEqualTo(tenant)
        verify(exactly = 1) { tenantRepository.findById(id) }
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { tenantRepository.findById(id) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
        verify(exactly = 1) { tenantRepository.findById(id) }
    }

    @Test
    fun `findAll delegates to findFiltered and returns TenantPage`() {
        val pageable = PageRequest.of(0, 10)
        val tenants =
            listOf(
                Tenant(
                    id = UUID.randomUUID(),
                    cnpj = "1",
                    tradeName = "A",
                    street = "S",
                    number = "1",
                    city = "C",
                    state = "SP",
                    zipCode = "01234567",
                ),
            )
        val springPage =
            org.springframework.data.domain
                .PageImpl(tenants, pageable, 1L)
        every { tenantRepository.findFiltered(null, pageable) } returns springPage

        val result = adapter.findAll(null, 0, 10)

        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.pageSize).isEqualTo(10)
        verify(exactly = 1) { tenantRepository.findFiltered(null, pageable) }
    }

    @Test
    fun `existsByCnpjExcludingId returns repository result when excludeId is null`() {
        every { tenantRepository.existsByCnpj("123") } returns true

        val result = adapter.existsByCnpjExcludingId("123", null)

        assertThat(result).isTrue()
        verify(exactly = 1) { tenantRepository.existsByCnpj("123") }
    }

    @Test
    fun `existsByCnpjExcludingId returns repository result when excludeId is set`() {
        val id = UUID.randomUUID()
        every { tenantRepository.existsByCnpjAndIdNot("123", id) } returns false

        val result = adapter.existsByCnpjExcludingId("123", id)

        assertThat(result).isFalse()
        verify(exactly = 1) { tenantRepository.existsByCnpjAndIdNot("123", id) }
    }

    @Test
    fun `findActiveOrderByTradeName maps projection to TenantIdName`() {
        val id = UUID.randomUUID()
        val proj = mockk<TenantListProjection>()
        every { proj.getId() } returns id
        every { proj.getTradeName() } returns "Church A"
        every { tenantRepository.findActiveIdAndTradeName() } returns listOf(proj)

        val result = adapter.findActiveOrderByTradeName()

        assertThat(result).containsExactly(TenantIdName(id = id, name = "Church A"))
        verify(exactly = 1) { tenantRepository.findActiveIdAndTradeName() }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { tenantRepository.deleteById(id) } just runs

        adapter.deleteById(id)

        verify(exactly = 1) { tenantRepository.deleteById(id) }
    }

    @Test
    fun `findTenantSchemaByEmail delegates to repository and returns info when found`() {
        val email = "admin@example.com"
        val tenantId = UUID.randomUUID()
        val schemaName = "tenant_12345678000190"
        val proj = mockk<TenantSchemaInfoProjection>()
        every { proj.getTenantId() } returns tenantId
        every { proj.getSchemaName() } returns schemaName
        every { tenantRepository.findTenantSchemaByEmail(email) } returns proj

        val result = adapter.findTenantSchemaByEmail(email)

        assertThat(result).isNotNull
        assertThat(result!!.tenantId).isEqualTo(tenantId)
        assertThat(result.schemaName).isEqualTo(schemaName)
        verify(exactly = 1) { tenantRepository.findTenantSchemaByEmail(email) }
    }

    @Test
    fun `findTenantSchemaByEmail returns null when repository returns null`() {
        val email = "unknown@example.com"
        every { tenantRepository.findTenantSchemaByEmail(email) } returns null

        val result = adapter.findTenantSchemaByEmail(email)

        assertThat(result).isNull()
        verify(exactly = 1) { tenantRepository.findTenantSchemaByEmail(email) }
    }

    @Test
    fun `findAllSchemaNames delegates to repository and returns list`() {
        val schemaNames = listOf("tenant_test", "tenant_foo")
        every { tenantRepository.findAllSchemaNames() } returns schemaNames

        val result = adapter.findAllSchemaNames()

        assertThat(result).isEqualTo(schemaNames)
        verify(exactly = 1) { tenantRepository.findAllSchemaNames() }
    }
}
