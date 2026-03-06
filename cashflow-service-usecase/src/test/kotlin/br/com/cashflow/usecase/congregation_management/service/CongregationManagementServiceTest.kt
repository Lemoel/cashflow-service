package br.com.cashflow.usecase.congregation_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.congregation.port.CongregationPage
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequest
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
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

class CongregationManagementServiceTest {
    private val congregationOutputPort: CongregationOutputPort = mockk()
    private val tenantOutputPort: TenantOutputPort = mockk()
    private lateinit var service: CongregationManagementService

    @BeforeEach
    fun setUp() {
        service = CongregationManagementService(congregationOutputPort, tenantOutputPort)
    }

    @Test
    fun `create returns saved congregation when tenant exists and CNPJ is unique`() {
        val tenantId = UUID.randomUUID()
        val request =
            CongregationCreateRequest(
                tenantId = tenantId,
                nome = "Cong A",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        val saved =
            Congregation(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                nome = "CONG A",
                logradouro = "Rua X",
                bairro = "CENTRO",
                numero = "1",
                cidade = "SÃO PAULO",
                uf = "SP",
                cep = "01234567",
            )
        every { tenantOutputPort.findById(tenantId) } returns
            Tenant(id = tenantId, tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { congregationOutputPort.existsByCnpjExcludingId(any(), null) } returns false
        every { congregationOutputPort.save(match { true }) } returns saved

        val result = service.create(request)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { congregationOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws BusinessException when tenant not found`() {
        val tenantId = UUID.randomUUID()
        val request =
            CongregationCreateRequest(
                tenantId = tenantId,
                nome = "Cong A",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        every { tenantOutputPort.findById(tenantId) } returns null

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("congregação vinculada")
        verify(exactly = 0) { congregationOutputPort.save(any()) }
    }

    @Test
    fun `create throws ConflictException when CNPJ already exists`() {
        val tenantId = UUID.randomUUID()
        val request =
            CongregationCreateRequest(
                tenantId = tenantId,
                nome = "Cong A",
                cnpj = "11222333000181",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        every { tenantOutputPort.findById(tenantId) } returns
            Tenant(id = tenantId, tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")
        every { congregationOutputPort.existsByCnpjExcludingId("11222333000181", null) } returns true

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe uma congregação com este CNPJ")
        verify(exactly = 0) { congregationOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when nome is blank`() {
        val tenantId = UUID.randomUUID()
        val request =
            CongregationCreateRequest(
                tenantId = tenantId,
                nome = "  ",
                logradouro = "Rua X",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        every { tenantOutputPort.findById(tenantId) } returns
            Tenant(id = tenantId, tradeName = "T", street = "S", number = "1", city = "C", state = "SP", zipCode = "01234567")

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("nome é obrigatório")
        verify(exactly = 0) { congregationOutputPort.save(any()) }
    }

    @Test
    fun `update returns updated congregation when found and CNPJ unique`() {
        val id = UUID.randomUUID()
        val existing =
            Congregation(
                id = id,
                tenantId = UUID.randomUUID(),
                nome = "OLD",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        val request =
            CongregationUpdateRequest(
                nome = "New Name",
                logradouro = "Rua",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        every { congregationOutputPort.findById(id) } returns existing
        every { congregationOutputPort.existsByCnpjExcludingId(any(), id) } returns false
        every { congregationOutputPort.save(match { true }) } answers { firstArg() }

        val result = service.update(id, request)

        assertThat(result.nome).isEqualTo("NEW NAME")
        verify(exactly = 1) { congregationOutputPort.save(match { true }) }
    }

    @Test
    fun `update throws ResourceNotFoundException when congregation not found`() {
        val id = UUID.randomUUID()
        every { congregationOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                CongregationUpdateRequest(
                    nome = "A",
                    logradouro = "R",
                    bairro = "B",
                    numero = "1",
                    cidade = "C",
                    uf = "SP",
                    cep = "01234567",
                ),
            )
        }.isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Congregação não encontrada")
    }

    @Test
    fun `findById returns congregation when found`() {
        val id = UUID.randomUUID()
        val cong =
            Congregation(
                id = id,
                tenantId = UUID.randomUUID(),
                nome = "A",
                logradouro = "R",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        every { congregationOutputPort.findById(id) } returns cong

        val result = service.findById(id)

        assertThat(result).isEqualTo(cong)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { congregationOutputPort.findById(id) } returns null

        assertThat(service.findById(id)).isNull()
    }

    @Test
    fun `findAll delegates to output port`() {
        val page = CongregationPage(emptyList(), 0L, 0, 10)
        every { congregationOutputPort.findAll(null, 0, 10) } returns page

        val result = service.findAll(null, 0, 10)

        assertThat(result).isEqualTo(page)
    }

    @Test
    fun `findListForDropdown maps to pairs`() {
        val id = UUID.randomUUID()
        val list =
            listOf(
                Congregation(
                    id = id,
                    tenantId = UUID.randomUUID(),
                    nome = "Cong",
                    logradouro = "R",
                    bairro = "B",
                    numero = "1",
                    cidade = "C",
                    uf = "SP",
                    cep = "01234567",
                ),
            )
        every { congregationOutputPort.findAllOrderByNome() } returns list

        val result = service.findListForDropdown()

        assertThat(result).containsExactly(id to "Cong")
    }

    @Test
    fun `findSetoriais delegates to output port`() {
        val id = UUID.randomUUID()
        val list =
            listOf(
                Congregation(
                    id = id,
                    tenantId = UUID.randomUUID(),
                    nome = "Setorial",
                    logradouro = "R",
                    bairro = "B",
                    numero = "1",
                    cidade = "C",
                    uf = "SP",
                    cep = "01234567",
                ),
            )
        every { congregationOutputPort.findSetoriais() } returns list

        val result = service.findSetoriais()

        assertThat(result).containsExactly(id to "Setorial")
    }

    @Test
    fun `delete throws ResourceNotFoundException when not found`() {
        val id = UUID.randomUUID()
        every { congregationOutputPort.findById(id) } returns null

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ResourceNotFoundException::class.java)
        verify(exactly = 0) { congregationOutputPort.deleteById(any()) }
    }

    @Test
    fun `delete calls deleteById when congregation exists`() {
        val id = UUID.randomUUID()
        val cong =
            Congregation(
                id = id,
                tenantId = UUID.randomUUID(),
                nome = "A",
                logradouro = "R",
                bairro = "B",
                numero = "1",
                cidade = "C",
                uf = "SP",
                cep = "01234567",
            )
        every { congregationOutputPort.findById(id) } returns cong
        every { congregationOutputPort.deleteById(id) } just runs

        service.delete(id)

        verify(exactly = 1) { congregationOutputPort.deleteById(id) }
    }

    @Test
    fun `isCnpjAvailable returns true when CNPJ not registered`() {
        every { congregationOutputPort.existsByCnpjExcludingId("11222333000181", null) } returns false

        val result = service.isCnpjAvailable("11222333000181", null)

        assertThat(result).isTrue()
    }

    @Test
    fun `isCnpjAvailable returns false when CNPJ already registered`() {
        every { congregationOutputPort.existsByCnpjExcludingId("11222333000181", null) } returns true

        val result = service.isCnpjAvailable("11222333000181", null)

        assertThat(result).isFalse()
    }
}
