package br.com.cashflow.tests.usecase.congregation_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.tests.config.TestTenantConfig
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequestDto
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequestDto
import br.com.cashflow.usecase.congregation_management.port.CongregationManagementInputPort
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@SqlSetUp(value = ["/db/scripts/tenant/load.sql"])
@SqlTearDown(value = ["/db/scripts/congregation/teardown.sql"])
class CongregationManagementServiceITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var congregationManagement: CongregationManagementInputPort

    @Autowired
    private lateinit var tenantManagement: TenantManagementInputPort

    private fun createTenant(): UUID = TestTenantConfig.TEST_TENANT_ID

    @Test
    fun should_CreateFindUpdateFindAllFindListForDropdownAndDelete_When_FullCrud() {
        // prepare
        val tenantId = createTenant()
        val createRequest =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                nome = "Congregação CRUD",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "100",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
                email = "cong@test.com",
                ativo = true,
            )

        // call
        val created = congregationManagement.create(createRequest)

        // assert
        assertThat(created.id).isNotNull()
        assertThat(created.tenantId).isEqualTo(tenantId)
        assertThat(created.nome).isEqualTo("CONGREGAÇÃO CRUD")
        assertThat(created.bairro).isEqualTo("CENTRO")
        assertThat(created.cidade).isEqualTo("SÃO PAULO")
        assertThat(created.email).isEqualTo("cong@test.com")
        assertThat(created.ativo).isTrue()

        // call
        val found = congregationManagement.findById(created.id!!)

        // assert
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.nome).isEqualTo("CONGREGAÇÃO CRUD")

        // prepare
        val updateRequest =
            CongregationUpdateRequestDto(
                nome = "Congregação CRUD Atualizada",
                logradouro = "Rua Y",
                bairro = "Bairro",
                numero = "200",
                cidade = "Campinas",
                uf = "SP",
                cep = "13000000",
                ativo = false,
            )

        // call
        val updated = congregationManagement.update(created.id!!, updateRequest)

        // assert
        assertThat(updated.nome).isEqualTo("CONGREGAÇÃO CRUD ATUALIZADA")
        assertThat(updated.cidade).isEqualTo("CAMPINAS")
        assertThat(updated.ativo).isFalse()

        // call
        val page = congregationManagement.findAll(null, 0, 10)

        // assert
        assertThat(page.items).isNotEmpty
        assertThat(page.total).isGreaterThanOrEqualTo(1)

        // call
        val listAll = congregationManagement.findListForDropdown()

        // assert
        assertThat(listAll.any { it.second == "CONGREGAÇÃO CRUD ATUALIZADA" }).isTrue()

        // call
        congregationManagement.delete(created.id!!)

        // call
        val afterDelete = congregationManagement.findById(created.id!!)

        // assert
        assertThat(afterDelete).isNull()
    }

    @Test
    fun should_ThrowBusinessException_When_CreateAndTenantNotFound() {
        // prepare
        val request =
            CongregationCreateRequestDto(
                tenantId = UUID.randomUUID(),
                nome = "Cong",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )

        // call & assert
        assertThatThrownBy { congregationManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Tenant não encontrado")
    }

    @Test
    fun should_ThrowConflictException_When_CreateWithDuplicateCnpj() {
        // prepare
        val tenantId = createTenant()
        val request =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                nome = "Cong 1",
                cnpj = "11222333000181",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        congregationManagement.create(request)

        // call & assert
        assertThatThrownBy { congregationManagement.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe uma congregação com este CNPJ")
    }

    @Test
    fun should_ReturnNull_When_FindByIdAndCongregationNotFound() {
        // call
        val result = congregationManagement.findById(UUID.randomUUID())

        // assert
        assertThat(result).isNull()
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_UpdateAndCongregationNotFound() {
        // prepare
        val request =
            CongregationUpdateRequestDto(
                nome = "A",
                logradouro = "R",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )

        // call & assert
        assertThatThrownBy { congregationManagement.update(UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Congregação não encontrada")
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_DeleteAndCongregationNotFound() {
        // call & assert
        assertThatThrownBy { congregationManagement.delete(UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Congregação não encontrada")
    }

    @Test
    fun should_ReturnMatchingCongregations_When_FindAllWithFilter() {
        // prepare
        val tenantId = createTenant()
        val request =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                nome = "Cong Filtro",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        congregationManagement.create(request)

        // call
        val page = congregationManagement.findAll(CongregationFilterModel(nome = "CONG FILTRO"), 0, 10)

        // assert
        assertThat(page.items).isNotEmpty
        assertThat(page.items.any { it.nome == "CONG FILTRO" }).isTrue()
    }

    @Test
    fun should_ReturnMatchingCongregations_When_FindAllWithPartialNomeFilter() {
        val tenantId = createTenant()
        val request =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                nome = "Cong Filtro",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        congregationManagement.create(request)

        val page = congregationManagement.findAll(CongregationFilterModel(nome = "FILTRO"), 0, 10)

        assertThat(page.items).isNotEmpty
        assertThat(page.items.any { it.nome == "CONG FILTRO" }).isTrue()
    }

    @Test
    fun should_ReturnOnlySetoriaisActiveWithNullSetorialId_When_FindSetoriais() {
        // prepare
        val tenantId = createTenant()
        val setorialRequest =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                nome = "Setorial A",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
                ativo = true,
            )
        val setorial = congregationManagement.create(setorialRequest)

        val filhaRequest =
            CongregationCreateRequestDto(
                tenantId = tenantId,
                setorialId = setorial.id,
                nome = "Cong Filha",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
                ativo = true,
            )
        congregationManagement.create(filhaRequest)

        // call
        val setoriais = congregationManagement.findSetoriais()

        // assert
        assertThat(setoriais.map { it.second }).contains("SETORIAL A")
        assertThat(setoriais.map { it.second }).doesNotContain("CONG FILHA")
    }
}
