package br.com.cashflow.usecase.tenant_management.service

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.tenant.port.TenantPage
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class TenantManagementServiceTest {
    private val tenantOutputPort: TenantOutputPort = mockk()
    private lateinit var service: TenantManagementService

    @BeforeEach
    fun setUp() {
        service = TenantManagementService(tenantOutputPort)
    }

    @Test
    fun `create returns saved tenant when CNPJ is unique`() {
        val request =
            TenantCreateRequest(
                tradeName = "Church A",
                cnpj = "12345678000190",
                street = "Street",
                number = "1",
                city = "City",
                state = "SP",
                zipCode = "01234567",
            )
        val saved =
            Tenant(
                id = UUID.randomUUID(),
                cnpj = "12345678000190",
                tradeName = "CHURCH A",
                street = "STREET",
                number = "1",
                city = "CITY",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns false
        every { tenantOutputPort.save(match { true }) } returns saved

        val result = service.create(request)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { tenantOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws ConflictException when CNPJ already exists`() {
        val request =
            TenantCreateRequest(
                tradeName = "Church A",
                cnpj = "12345678000190",
                street = "Street",
                number = "1",
                city = "City",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns true

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ already registered")
        verify(exactly = 0) { tenantOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws when CNPJ has wrong digit count after normalization`() {
        val request =
            TenantCreateRequest(
                tradeName = "Church A",
                cnpj = "123",
                street = "Street",
                number = "1",
                city = "City",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantOutputPort.existsByCnpjExcludingId("123", null) } returns false

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("14 digits")
    }

    @Test
    fun `update returns updated tenant when CNPJ is unique`() {
        val id = UUID.randomUUID()
        val existing =
            Tenant(id = id, cnpj = "12345678000190", tradeName = "OLD", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        val request =
            TenantUpdateRequest(
                tradeName = "New Name",
                cnpj = "12345678000190",
                street = "Street",
                number = "1",
                city = "City",
                state = "SP",
                zipCode = "01234567",
            )
        every { tenantOutputPort.findById(id) } returns existing
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", id) } returns false
        every { tenantOutputPort.save(match { true }) } answers { firstArg() }

        val result = service.update(id, request)

        assertThat(result.tradeName).isEqualTo("NEW NAME")
        verify(exactly = 1) { tenantOutputPort.save(match { true }) }
    }

    @Test
    fun `update throws ResourceNotFoundException when tenant not found`() {
        val id = UUID.randomUUID()
        every { tenantOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                TenantUpdateRequest(
                    tradeName = "A",
                    cnpj = "12345678000190",
                    street = "S",
                    number = "1",
                    city = "C",
                    state = "SP",
                    zipCode = "01234567",
                ),
            )
        }.isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun `update throws ConflictException when CNPJ belongs to another tenant`() {
        val id = UUID.randomUUID()
        val existing =
            Tenant(id = id, cnpj = "11111111111111", tradeName = "A", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { tenantOutputPort.findById(id) } returns existing
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", id) } returns true

        assertThatThrownBy {
            service.update(
                id,
                TenantUpdateRequest(
                    tradeName = "A",
                    cnpj = "12.345.678/0001-90",
                    street = "S",
                    number = "1",
                    city = "C",
                    state = "SP",
                    zipCode = "01234567",
                ),
            )
        }.isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ already registered")
    }

    @Test
    fun `findById returns tenant when found`() {
        val id = UUID.randomUUID()
        val tenant =
            Tenant(id = id, cnpj = "12345678000190", tradeName = "A", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { tenantOutputPort.findById(id) } returns tenant

        val result = service.findById(id)

        assertThat(result).isEqualTo(tenant)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { tenantOutputPort.findById(id) } returns null

        val result = service.findById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `findAll delegates to output port`() {
        val page = TenantPage(emptyList(), 0L, 0, 10)
        every { tenantOutputPort.findAll(any(), 0, 10) } returns page

        val result = service.findAll(null, 0, 10)

        assertThat(result).isEqualTo(page)
        verify(exactly = 1) { tenantOutputPort.findAll(null, 0, 10) }
    }

    @Test
    fun `findActiveForList delegates to output port`() {
        val list =
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
        every { tenantOutputPort.findActiveOrderByTradeName() } returns list

        val result = service.findActiveForList()

        assertThat(result).isEqualTo(list)
    }

    @Test
    fun `delete throws ResourceNotFoundException when tenant not found`() {
        val id = UUID.randomUUID()
        every { tenantOutputPort.findById(id) } returns null

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `delete calls deleteById when tenant exists`() {
        val id = UUID.randomUUID()
        val tenant = Tenant(id = id, cnpj = "1", tradeName = "A", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { tenantOutputPort.findById(id) } returns tenant
        every { tenantOutputPort.deleteById(id) } just runs

        service.delete(id)

        verify(exactly = 1) { tenantOutputPort.deleteById(id) }
    }

    @Test
    fun `isCnpjAvailable returns true when CNPJ is not registered`() {
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000195", null) } returns false

        val result = service.isCnpjAvailable("12345678000195", null)

        assertThat(result).isTrue()
    }

    @Test
    fun `isCnpjAvailable returns false when CNPJ is already registered`() {
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000195", null) } returns true

        val result = service.isCnpjAvailable("12345678000195", null)

        assertThat(result).isFalse()
    }

    @Test
    fun `isCnpjAvailable returns true when CNPJ exists but is the excluded tenant`() {
        val excludeId = UUID.randomUUID()
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000195", excludeId) } returns false

        val result = service.isCnpjAvailable("12345678000195", excludeId)

        assertThat(result).isTrue()
    }

    @Test
    fun `isCnpjAvailable normalizes masked CNPJ`() {
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000195", null) } returns false

        val result = service.isCnpjAvailable("12.345.678/0001-95", null)

        assertThat(result).isTrue()
        verify(exactly = 1) { tenantOutputPort.existsByCnpjExcludingId("12345678000195", null) }
    }

    @Test
    fun `isCnpjAvailable throws when CNPJ has wrong digit count after normalization`() {
        assertThatThrownBy { service.isCnpjAvailable("123", null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("14 digits")
        verify(exactly = 0) { tenantOutputPort.existsByCnpjExcludingId(any(), any()) }
    }
}
