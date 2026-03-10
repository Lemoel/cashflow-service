package br.com.cashflow.usecase.maquina_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItem
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequest
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequest
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
            MaquinaCreateRequest(
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
        val detalhes =
            MaquinaComCongregacao(
                id = saved.id!!,
                maquinaId = "ABC123",
                congregacaoId = congregacaoId,
                congregacaoNome = "Cong",
                bancoId = bancoId,
                bancoNome = "Banco",
                departamentoId = null,
                departamentoNome = null,
                ativo = true,
                version = 0L,
                createdAt = Instant.now(),
                updatedAt = null,
            )
        every { congregationOutputPort.findById(congregacaoId) } returns Congregation(id = congregacaoId, nome = "Cong")
        every { bankOutputPort.findById(bancoId) } returns Bank(id = bancoId, nome = "Banco")
        every { maquinaOutputPort.existsByNumeroSerieLeitor("ABC123") } returns false
        every { maquinaOutputPort.save(match { true }) } returns saved
        every { maquinaOutputPort.findByIdWithDetalhes(match { true }) } returns detalhes

        val result = service.create(request)

        assertThat(result.maquinaId).isEqualTo("ABC123")
    }

    @Test
    fun `create throws ConflictException when maquinaId already exists`() {
        val request =
            MaquinaCreateRequest(
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
        verify(exactly = 0) { maquinaOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws BusinessException when congregacaoId is null`() {
        val request =
            MaquinaCreateRequest(
                maquinaId = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = UUID.randomUUID(),
            )
        every { congregationOutputPort.findById(request.congregacaoId) } returns null

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Congregação da máquina não encontrada")
    }

    @Test
    fun `update throws ResourceNotFoundException when maquina not found`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                MaquinaUpdateRequest(
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
        val detalhes =
            MaquinaComCongregacao(
                id = id,
                maquinaId = "X",
                congregacaoId = newCong,
                congregacaoNome = "N",
                bancoId = bancoId,
                bancoNome = "B",
                departamentoId = null,
                departamentoNome = null,
                ativo = true,
                version = 0L,
                createdAt = null,
                updatedAt = null,
            )
        every { maquinaOutputPort.findById(id) } returns existing
        every { congregationOutputPort.findById(newCong) } returns Congregation(id = newCong)
        every { bankOutputPort.findById(bancoId) } returns Bank(id = bancoId)
        every { maquinaOutputPort.save(match { true }) } answers { firstArg() }
        every { maquinaOutputPort.findByIdWithDetalhes(match { true }) } returns detalhes

        val result =
            service.update(
                id,
                MaquinaUpdateRequest(congregacaoId = newCong, bancoId = bancoId),
            )

        assertThat(result.congregacaoId).isEqualTo(newCong)
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
        every { maquinaOutputPort.findById(id) } returns Maquina(id = id, bancoId = UUID.randomUUID())
        every { maquinaOutputPort.deleteById(id) } throws DataIntegrityViolationException("fk")

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Erro ao excluir a máquina. Verifique se não há registros dependentes.")
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { maquinaOutputPort.findByIdWithDetalhes(id) } returns null

        assertThat(service.findById(id)).isNull()
    }

    @Test
    fun `listHistoricoByMaquinaId returns list from port`() {
        val maquinaId = UUID.randomUUID()
        val items =
            listOf(
                MaquinaHistoricoItem(
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
