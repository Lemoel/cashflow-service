package br.com.cashflow.tests.usecase.tenant_management.service

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequest
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@SqlSetUp(value = ["/db/scripts/tenant/load.sql"])
@SqlTearDown(value = ["/db/scripts/tenant/teardown.sql"])
class TenantManagementServiceIT : PostgresqlBaseTest() {

    @Autowired
    private lateinit var tenantManagementInputPort: TenantManagementInputPort

    @Test
    fun `create findById update findAll findActiveForList delete full CRUD`() {
        val createRequest =
            TenantCreateRequest(
                tradeName = "Church CRUD",
                companyName = "Company CRUD",
                cnpj = "12345678000190",
                street = "Street",
                number = "1",
                complement = "Room 1",
                neighborhood = "Center",
                city = "City",
                state = "SP",
                zipCode = "01234567",
                phone = "11999999999",
                email = "church@test.com",
                active = true,
            )

        val created = tenantManagementInputPort.create(createRequest)

        assertThat(created.id).isNotNull()
        assertThat(created.cnpj).isEqualTo("12345678000190")
        assertThat(created.tradeName).isEqualTo("CHURCH CRUD")
        assertThat(created.companyName).isEqualTo("COMPANY CRUD")
        assertThat(created.street).isEqualTo("STREET")
        assertThat(created.number).isEqualTo("1")
        assertThat(created.city).isEqualTo("CITY")
        assertThat(created.state).isEqualTo("SP")
        assertThat(created.active).isTrue()

        val found = tenantManagementInputPort.findById(created.id!!)
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.tradeName).isEqualTo("CHURCH CRUD")

        val updateRequest =
            TenantUpdateRequest(
                tradeName = "Church CRUD Updated",
                companyName = "Company Updated",
                cnpj = "12345678000190",
                street = "New Street",
                number = "2",
                complement = null,
                neighborhood = "North",
                city = "New City",
                state = "RJ",
                zipCode = "20000000",
                phone = null,
                email = "updated@test.com",
                active = false,
            )

        val updated = tenantManagementInputPort.update(created.id!!, updateRequest)

        assertThat(updated.tradeName).isEqualTo("CHURCH CRUD UPDATED")
        assertThat(updated.state).isEqualTo("RJ")
        assertThat(updated.active).isFalse()

        val page = tenantManagementInputPort.findAll(null, 0, 10)
        assertThat(page.items).isNotEmpty
        assertThat(page.total).isGreaterThanOrEqualTo(1)

        val listActive = tenantManagementInputPort.findActiveForList()
        val createdInList = listActive.find { it.id == created.id }
        assertThat(createdInList).isNull()

        tenantManagementInputPort.delete(created.id!!)

        val afterDelete = tenantManagementInputPort.findById(created.id!!)
        assertThat(afterDelete).isNull()
    }

    @Test
    fun `create with duplicate CNPJ throws ConflictException`() {
        val request =
            TenantCreateRequest(
                tradeName = "First",
                cnpj = "11111111000191",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        tenantManagementInputPort.create(request)

        assertThatThrownBy { tenantManagementInputPort.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ already registered")
    }

    @Test
    fun `findById returns null when not found`() {
        val result = tenantManagementInputPort.findById(java.util.UUID.randomUUID())
        assertThat(result).isNull()
    }

    @Test
    fun `update when tenant not found throws ResourceNotFoundException`() {
        val request =
            TenantUpdateRequest(
                tradeName = "A",
                cnpj = "12345678000190",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )

        assertThatThrownBy { tenantManagementInputPort.update(java.util.UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun `delete when tenant not found throws ResourceNotFoundException`() {
        assertThatThrownBy { tenantManagementInputPort.delete(java.util.UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun `findActiveForList returns only active tenants ordered by tradeName`() {
        val a =
            TenantCreateRequest(
                tradeName = "Zebra Church",
                cnpj = "22222222000192",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
                active = true,
            )
        val b =
            TenantCreateRequest(
                tradeName = "Alpha Church",
                cnpj = "33333333000193",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
                active = true,
            )
        tenantManagementInputPort.create(a)
        tenantManagementInputPort.create(b)

        val list = tenantManagementInputPort.findActiveForList()

        assertThat(list).isNotEmpty
        val names = list.map { it.tradeName }
        assertThat(names.indexOf("ALPHA CHURCH")).isLessThan(names.indexOf("ZEBRA CHURCH"))
    }

    @Test
    fun `isCnpjAvailable returns true when CNPJ is not registered`() {
        val result = tenantManagementInputPort.isCnpjAvailable("99999999000199", null)
        assertThat(result).isTrue()
    }

    @Test
    fun `isCnpjAvailable returns false when CNPJ is already registered`() {
        val createRequest =
            TenantCreateRequest(
                tradeName = "Unique Check",
                cnpj = "12345678000190",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        tenantManagementInputPort.create(createRequest)

        val result = tenantManagementInputPort.isCnpjAvailable("12345678000190", null)

        assertThat(result).isFalse()
    }

    @Test
    fun `isCnpjAvailable returns true when CNPJ exists but excludeId is that tenant`() {
        val createRequest =
            TenantCreateRequest(
                tradeName = "Exclude Self",
                cnpj = "12345678000190",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        val created = tenantManagementInputPort.create(createRequest)

        val result = tenantManagementInputPort.isCnpjAvailable("12345678000190", created.id)

        assertThat(result).isTrue()
    }

    @Test
    fun `isCnpjAvailable normalizes masked CNPJ`() {
        val createRequest =
            TenantCreateRequest(
                tradeName = "Masked",
                cnpj = "12345678000190",
                street = "S",
                number = "1",
                city = "C",
                state = "SP",
                zipCode = "01234567",
            )
        tenantManagementInputPort.create(createRequest)

        val result = tenantManagementInputPort.isCnpjAvailable("12.345.678/0001-90", null)

        assertThat(result).isFalse()
    }
}
