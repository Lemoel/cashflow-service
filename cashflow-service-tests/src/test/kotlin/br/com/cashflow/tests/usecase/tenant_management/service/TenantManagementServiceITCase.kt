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
import java.util.UUID

@SqlSetUp(value = ["/db/scripts/tenant/load.sql"])
@SqlTearDown(value = ["/db/scripts/tenant/teardown.sql"])
class TenantManagementServiceITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var tenantManagementInputPort: TenantManagementInputPort

    @Test
    fun should_CreateFindUpdateFindAllFindActiveForListAndDelete_When_FullCrud() {
        // prepare
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

        // call
        val created = tenantManagementInputPort.create(createRequest)

        // assert
        assertThat(created.id).isNotNull()
        assertThat(created.cnpj).isEqualTo("12345678000190")
        assertThat(created.tradeName).isEqualTo("CHURCH CRUD")
        assertThat(created.companyName).isEqualTo("COMPANY CRUD")
        assertThat(created.street).isEqualTo("STREET")
        assertThat(created.number).isEqualTo("1")
        assertThat(created.city).isEqualTo("CITY")
        assertThat(created.state).isEqualTo("SP")
        assertThat(created.active).isTrue()

        // call
        val found = tenantManagementInputPort.findById(created.id!!)

        // assert
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.tradeName).isEqualTo("CHURCH CRUD")

        // prepare
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

        // call
        val updated = tenantManagementInputPort.update(created.id!!, updateRequest)

        // assert
        assertThat(updated.tradeName).isEqualTo("CHURCH CRUD UPDATED")
        assertThat(updated.state).isEqualTo("RJ")
        assertThat(updated.active).isFalse()

        // call
        val page = tenantManagementInputPort.findAll(null, 0, 10)

        // assert
        assertThat(page.items).isNotEmpty
        assertThat(page.total).isGreaterThanOrEqualTo(1)

        // call
        val listActive = tenantManagementInputPort.findActiveForList()
        val createdInList = listActive.find { it.id == created.id }

        // assert (inactive tenant must not appear in list for dropdown)
        assertThat(createdInList).isNull()

        // call
        tenantManagementInputPort.delete(created.id!!)

        // call
        val afterDelete = tenantManagementInputPort.findById(created.id!!)

        // assert
        assertThat(afterDelete).isNull()
    }

    @Test
    fun should_ThrowConflictException_When_CreateWithDuplicateCnpj() {
        // prepare
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

        // call & assert
        assertThatThrownBy { tenantManagementInputPort.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ already registered")
    }

    @Test
    fun should_ReturnNull_When_FindByIdAndTenantNotFound() {
        // prepare
        val id = UUID.randomUUID()

        // call
        val result = tenantManagementInputPort.findById(id)

        // assert
        assertThat(result).isNull()
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_UpdateAndTenantNotFound() {
        // prepare
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

        // call & assert
        assertThatThrownBy { tenantManagementInputPort.update(UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_DeleteAndTenantNotFound() {
        // call & assert
        assertThatThrownBy { tenantManagementInputPort.delete(UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun should_ReturnOnlyActiveTenantsOrderedByTradeName_When_FindActiveForList() {
        // prepare
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

        // call
        val list = tenantManagementInputPort.findActiveForList()

        // assert
        assertThat(list).isNotEmpty
        val names = list.map { it.tradeName }
        assertThat(names.indexOf("ALPHA CHURCH")).isLessThan(names.indexOf("ZEBRA CHURCH"))
    }

    @Test
    fun should_ReturnTrue_When_IsCnpjAvailableAndCnpjNotRegistered() {
        // call
        val result = tenantManagementInputPort.isCnpjAvailable("99999999000199", null)

        // assert
        assertThat(result).isTrue()
    }

    @Test
    fun should_ReturnFalse_When_IsCnpjAvailableAndCnpjAlreadyRegistered() {
        // prepare
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

        // call
        val result = tenantManagementInputPort.isCnpjAvailable("12345678000190", null)

        // assert
        assertThat(result).isFalse()
    }

    @Test
    fun should_ReturnTrue_When_IsCnpjAvailableAndExcludeIdIsThatTenant() {
        // prepare
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

        // call
        val result = tenantManagementInputPort.isCnpjAvailable("12345678000190", created.id)

        // assert
        assertThat(result).isTrue()
    }

    @Test
    fun should_NormalizeMaskedCnpj_When_IsCnpjAvailable() {
        // prepare
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

        // call
        val result = tenantManagementInputPort.isCnpjAvailable("12.345.678/0001-90", null)

        // assert
        assertThat(result).isFalse()
    }
}
