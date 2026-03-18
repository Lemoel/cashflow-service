package br.com.cashflow.usecase.maquina_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.util.UUID

class MaquinaManagementServiceTest {
    private val maquinaOutputPort: MaquinaOutputPort = mockk()
    private val maquinaHistoricoOutputPort: MaquinaHistoricoOutputPort = mockk(relaxed = true)
    private val congregationOutputPort: CongregationOutputPort = mockk()
    private val departmentOutputPort: DepartmentOutputPort = mockk()
    private val bankOutputPort: BankOutputPort = mockk()
    private lateinit var service: MaquinaManagementService

    @BeforeEach
    fun setUp() {
        service =
            MaquinaManagementService(
                maquinaOutputPort,
                maquinaHistoricoOutputPort,
                congregationOutputPort,
                departmentOutputPort,
                bankOutputPort,
            )
    }

    @Test
    fun `create returns saved maquina when valid`() {
        val congregacaoId = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val request =
            MaquinaCreateRequestDto(
                maquinaId = " abc123 ",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
                departamentoId = null,
                ativo = true,
            )
        val saved =
            Maquina(
                id = UUID.randomUUID(),
                numeroSerieLeitor = "ABC123",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
                departamentoId = null,
                ativo = true,
            )
        every { congregationOutputPort.findById(congregacaoId) } returns
            Congregation(id = congregacaoId, nome = "Cong")
        every { bankOutputPort.findById(bancoId) } returns Bank(id = bancoId, nome = "Banco")
        every { maquinaOutputPort.existsByNumeroSerieLeitor("ABC123") } returns false
        every { maquinaOutputPort.save(any()) } returns saved

        val result = service.create(request)

        assertThat(result.maquinaId).isEqualTo("ABC123")
        assertThat(result.congregacaoNome).isEqualTo("Cong")
        assertThat(result.bancoNome).isEqualTo("Banco")
    }

    @Test
    fun `create throws ConflictException when maquinaId already exists`() {
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "XYZ",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )
        every { congregationOutputPort.findById(any()) } returns Congregation()
        every { bankOutputPort.findById(any()) } returns Bank()
        every { maquinaOutputPort.existsByNumeroSerieLeitor("XYZ") } returns true

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe uma máquina com este ID")
        verify(exactly = 0) { maquinaOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when congregacao not found`() {
        val congregacaoId = UUID.randomUUID()
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "X",
                congregacaoId = congregacaoId,
                bancoId = UUID.randomUUID(),
            )
        every { congregationOutputPort.findById(congregacaoId) } returns null

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Congregação da máquina não encontrada")
    }

    @Test
    fun `create throws BusinessException when banco not found`() {
        val congregacaoId = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "X",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
            )
        every { congregationOutputPort.findById(congregacaoId) } returns Congregation(id = congregacaoId, nome = "C")
        every { bankOutputPort.findById(bancoId) } returns null

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Banco não encontrado")
    }

    @Test
    fun `create throws BusinessException when department belongs to different tenant`() {
        val congregacaoId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val departamentoId = UUID.randomUUID()
        val otherTenantId = UUID.randomUUID()
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "X",
                congregacaoId = congregacaoId,
                bancoId = UUID.randomUUID(),
                departamentoId = departamentoId,
                ativo = true,
            )
        every { congregationOutputPort.findById(congregacaoId) } returns
            Congregation(id = congregacaoId, tenantId = tenantId, nome = "C")
        every { bankOutputPort.findById(any()) } returns Bank(id = UUID.randomUUID(), nome = "B")
        every { departmentOutputPort.findById(departamentoId) } returns
            Department(id = departamentoId, tenantId = otherTenantId, nome = "D")

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("deve pertencer ao mesmo tenant")
    }

    @Test
    fun `update throws ResourceNotFoundException when maquina not found`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                MaquinaUpdateRequestDto(
                    congregacaoId = UUID.randomUUID(),
                    bancoId = UUID.randomUUID(),
                ),
            )
        }.isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Máquina não encontrada")
    }

    @Test
    fun `update calls fecharPeriodoAtual and inserirPeriodo when congregacao changes`() {
        val id = UUID.randomUUID()
        val oldCong = UUID.randomUUID()
        val newCong = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val existing =
            Maquina(
                id = id,
                numeroSerieLeitor = "X",
                congregacaoId = oldCong,
                bancoId = bancoId,
                departamentoId = null,
                ativo = true,
            )
        every { maquinaOutputPort.findById(id) } returns existing
        every { congregationOutputPort.findById(newCong) } returns Congregation(id = newCong, nome = "N")
        every { bankOutputPort.findById(bancoId) } returns Bank(id = bancoId, nome = "B")
        every { maquinaOutputPort.save(any()) } answers { firstArg() }

        val result =
            service.update(
                id,
                MaquinaUpdateRequestDto(congregacaoId = newCong, bancoId = bancoId),
            )

        assertThat(result.congregacaoId).isEqualTo(newCong)
        assertThat(result.congregacaoNome).isEqualTo("N")
    }

    @Test
    fun `update does not call fecharPeriodoAtual when congregacao and departamento unchanged`() {
        val id = UUID.randomUUID()
        val congregacaoId = UUID.randomUUID()
        val bancoId = UUID.randomUUID()
        val existing =
            Maquina(
                id = id,
                numeroSerieLeitor = "X",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
                departamentoId = null,
                ativo = true,
            )
        every { maquinaOutputPort.findById(id) } returns existing
        every { congregationOutputPort.findById(congregacaoId) } returns Congregation(id = congregacaoId, nome = "C")
        every { bankOutputPort.findById(bancoId) } returns Bank(id = bancoId, nome = "B")
        every { maquinaOutputPort.save(any()) } answers { firstArg() }

        service.update(id, MaquinaUpdateRequestDto(congregacaoId = congregacaoId, bancoId = bancoId, ativo = false))

        verify(exactly = 0) { maquinaHistoricoOutputPort.fecharPeriodoAtual(any()) }
        verify(exactly = 0) { maquinaHistoricoOutputPort.inserirPeriodo(any(), any(), any()) }
    }

    @Test
    fun `delete throws ResourceNotFoundException when maquina not found`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findById(id) } returns null

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Máquina não encontrada")
        verify(exactly = 0) { maquinaOutputPort.deleteById(any()) }
    }

    @Test
    fun `delete throws ConflictException when DataIntegrityViolationException`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findById(id) } returns
            Maquina(id = id, bancoId = UUID.randomUUID())
        every { maquinaOutputPort.deleteById(id) } throws DataIntegrityViolationException("fk")

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining(
                "Erro ao excluir a máquina. Verifique se não há registros dependentes.",
            )
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findByIdWithDetalhes(id) } returns null

        assertThat(service.findById(id)).isNull()
    }

    @Test
    fun `search delegates to maquinaOutputPort findWithFiltersComDetalhes`() {
        val pageResult = MaquinaPage(items = emptyList(), total = 0L, page = 0, pageSize = 10)
        every { maquinaOutputPort.findWithFiltersComDetalhes(any(), any(), any(), any(), 0, 10) } returns pageResult

        val result = service.search("x", "y", "z", null, 0, 10)

        assertThat(result.total).isEqualTo(0L)
        assertThat(result.items).isEmpty()
    }

    @Test
    fun `listForOptions delegates to maquinaOutputPort findParaSelecaoHistorico`() {
        val pageResult = MaquinaPage(items = emptyList(), total = 0L, page = 0, pageSize = 10)
        every { maquinaOutputPort.findParaSelecaoHistorico(any(), any(), any(), 0, 10) } returns pageResult

        val result = service.listForOptions(null, null, null, 0, 10)

        assertThat(result.total).isEqualTo(0L)
    }

    @Test
    fun `listOrSearch returns search result when search filters present`() {
        val pageResult = MaquinaPage(items = emptyList(), total = 1L, page = 0, pageSize = 10)
        every { maquinaOutputPort.findWithFiltersComDetalhes(any(), any(), any(), any(), 0, 10) } returns pageResult

        val result = service.listOrSearch("abc", null, null, null, null, null, null, 0, 10)

        assertThat(result.total).isEqualTo(1L)
    }

    @Test
    fun `listOrSearch returns listForOptions when no search filters`() {
        val pageResult = MaquinaPage(items = emptyList(), total = 0L, page = 0, pageSize = 10)
        every { maquinaOutputPort.findParaSelecaoHistorico(any(), any(), any(), 0, 10) } returns pageResult

        val result = service.listOrSearch(null, null, null, null, null, null, null, 0, 10)

        assertThat(result.total).isEqualTo(0L)
    }

    @Test
    fun `delete succeeds when maquina exists`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findById(id) } returns Maquina(id = id, bancoId = UUID.randomUUID())
        every { maquinaOutputPort.deleteById(id) } returns Unit

        service.delete(id)

        verify(exactly = 1) { maquinaOutputPort.deleteById(id) }
    }

    @Test
    fun `listHistoricoByMaquinaId returns list from port`() {
        val maquinaId = UUID.randomUUID()
        val items =
            listOf(
                MaquinaHistoricoItemModel(
                    UUID.randomUUID(),
                    maquinaId,
                    UUID.randomUUID(),
                    "Cong",
                    null,
                    null,
                    Instant.now(),
                    null,
                ),
            )
        every { maquinaHistoricoOutputPort.listarPorMaquinaId(maquinaId) } returns items

        assertThat(service.listHistoricoByMaquinaId(maquinaId)).isEqualTo(items)
    }
}
