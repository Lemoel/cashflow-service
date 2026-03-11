package br.com.cashflow.usecase.parametro_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroPageModel
import br.com.cashflow.usecase.parametro.port.ParametroOutputPort
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.EnumTipoParametro
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequestDto
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

class ParametroManagementServiceTest {
    private val parametroOutputPort: ParametroOutputPort = mockk()
    private lateinit var service: ParametroManagementService

    @BeforeEach
    fun setUp() {
        service = ParametroManagementService(parametroOutputPort)
    }

    @Test
    fun `create returns saved parametro when chave and valor valid and chave unique`() {
        val request =
            ParametroCreateRequestDto(
                chave = " CHAVE_A ",
                valor = "texto",
                tipo = EnumTipoParametro.TEXTO,
                ativo = true,
            )
        val saved =
            Parametro(
                id = UUID.randomUUID(),
                chave = "CHAVE_A",
                valorTexto = "texto",
                tipo = "STRING",
                ativo = true,
            )
        every { parametroOutputPort.existsByChave("CHAVE_A") } returns false
        every {
            parametroOutputPort.save(match { it.chave == "CHAVE_A" && it.valorTexto == "texto" })
        } returns saved

        val result = service.create(request)

        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { parametroOutputPort.save(match { true }) }
    }

    @Test
    fun `create throws ConflictException when chave already exists`() {
        val request =
            ParametroCreateRequestDto(
                chave = "CHAVE_X",
                valor = "v",
                tipo = EnumTipoParametro.TEXTO,
            )
        every { parametroOutputPort.existsByChave("CHAVE_X") } returns true

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um parâmetro com esta chave")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when chave is blank`() {
        val request =
            ParametroCreateRequestDto(
                chave = "  ",
                valor = "v",
                tipo = EnumTipoParametro.TEXTO,
            )

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("chave é obrigatória")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when valor is blank`() {
        val request =
            ParametroCreateRequestDto(
                chave = "CHAVE",
                valor = "  ",
                tipo = EnumTipoParametro.TEXTO,
            )
        every { parametroOutputPort.existsByChave("CHAVE") } returns false

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("valor é obrigatório")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `create throws BusinessException when tipo INTEIRO and valor not numeric`() {
        val request =
            ParametroCreateRequestDto(
                chave = "K",
                valor = "abc",
                tipo = EnumTipoParametro.INTEIRO,
            )
        every { parametroOutputPort.existsByChave("K") } returns false

        assertThatThrownBy { service.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Valor deve ser numérico")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `update with tipo INTEIRO sets valorInteiro`() {
        val id = UUID.randomUUID()
        val existing =
            Parametro(
                id = id,
                chave = "K",
                valorTexto = "old",
                tipo = "STRING",
                ativo = true,
            )
        val request =
            ParametroUpdateRequestDto(
                chave = "K",
                valor = "99",
                tipo = EnumTipoParametro.INTEIRO,
                ativo = true,
            )
        every { parametroOutputPort.findById(id) } returns existing
        every { parametroOutputPort.existsByChaveExcludingId("K", id) } returns false
        every { parametroOutputPort.save(match { it.valorInteiro == 99L }) } answers { firstArg() }

        val result = service.update(id, request)

        assertThat(result.valorInteiro).isEqualTo(99L)
        assertThat(result.valorTexto).isNull()
        assertThat(result.valorDecimal).isNull()
    }

    @Test
    fun `create accepts tipo DECIMAL with numeric valor`() {
        val request =
            ParametroCreateRequestDto(
                chave = "K",
                valor = "3.14",
                tipo = EnumTipoParametro.DECIMAL,
            )
        val saved =
            Parametro(
                id = UUID.randomUUID(),
                chave = "K",
                valorDecimal = 3.14,
                tipo = "DOUBLE",
                ativo = true,
            )
        every { parametroOutputPort.existsByChave("K") } returns false
        every { parametroOutputPort.save(match { it.valorDecimal == 3.14 }) } returns saved

        val result = service.create(request)

        assertThat(result).isEqualTo(saved)
    }

    @Test
    fun `update returns updated parametro when found and chave unique`() {
        val id = UUID.randomUUID()
        val existing =
            Parametro(
                id = id,
                chave = "OLD",
                valorTexto = "v",
                tipo = "STRING",
                ativo = true,
            )
        val request =
            ParametroUpdateRequestDto(
                chave = "NEW_CHAVE",
                valor = "novo",
                tipo = EnumTipoParametro.TEXTO,
                ativo = false,
            )
        every { parametroOutputPort.findById(id) } returns existing
        every { parametroOutputPort.existsByChaveExcludingId("NEW_CHAVE", id) } returns false
        every { parametroOutputPort.save(match { true }) } answers { firstArg() }

        val result = service.update(id, request)

        assertThat(result.chave).isEqualTo("NEW_CHAVE")
        assertThat(result.valorTexto).isEqualTo("novo")
        assertThat(result.ativo).isFalse()
        verify(exactly = 1) { parametroOutputPort.save(match { true }) }
    }

    @Test
    fun `update throws ResourceNotFoundException when id not found`() {
        val id = UUID.randomUUID()
        every { parametroOutputPort.findById(id) } returns null

        assertThatThrownBy {
            service.update(
                id,
                ParametroUpdateRequestDto(chave = "K", valor = "v", tipo = EnumTipoParametro.TEXTO),
            )
        }.isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Parâmetro não encontrado")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `update throws ConflictException when chave changed and already exists`() {
        val id = UUID.randomUUID()
        val existing =
            Parametro(id = id, chave = "OLD", valorTexto = "v", tipo = "STRING", ativo = true)
        val request =
            ParametroUpdateRequestDto(chave = "OTHER", valor = "v", tipo = EnumTipoParametro.TEXTO)
        every { parametroOutputPort.findById(id) } returns existing
        every { parametroOutputPort.existsByChaveExcludingId("OTHER", id) } returns true

        assertThatThrownBy { service.update(id, request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um parâmetro com esta chave")
        verify(exactly = 0) { parametroOutputPort.save(any()) }
    }

    @Test
    fun `findById returns parametro when found`() {
        val id = UUID.randomUUID()
        val p = Parametro(id = id, chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)
        every { parametroOutputPort.findById(id) } returns p

        val result = service.findById(id)

        assertThat(result).isEqualTo(p)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { parametroOutputPort.findById(id) } returns null

        assertThat(service.findById(id)).isNull()
    }

    @Test
    fun `findAll delegates to output port`() {
        val page = ParametroPageModel(emptyList(), 0L, 0, 10)
        every { parametroOutputPort.findWithFilters(null, 0, 10) } returns page

        val result = service.findAll(null, 0, 10)

        assertThat(result).isEqualTo(page)
    }

    @Test
    fun `findChavesForDropdown returns pairs chave chave`() {
        val list =
            listOf(
                Parametro(
                    id = UUID.randomUUID(),
                    chave = "A",
                    valorTexto = "1",
                    tipo = "STRING",
                    ativo = true,
                ),
                Parametro(
                    id = UUID.randomUUID(),
                    chave = "B",
                    valorTexto = "2",
                    tipo = "STRING",
                    ativo = true,
                ),
            )
        every { parametroOutputPort.findAllOrderByChave() } returns list

        val result = service.findChavesForDropdown()

        assertThat(result).containsExactly("A" to "A", "B" to "B")
    }

    @Test
    fun `delete throws ResourceNotFoundException when not found`() {
        val id = UUID.randomUUID()
        every { parametroOutputPort.findById(id) } returns null

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Parâmetro não encontrado")
        verify(exactly = 0) { parametroOutputPort.deleteById(any()) }
    }

    @Test
    fun `delete calls deleteById when parametro exists`() {
        val id = UUID.randomUUID()
        val p = Parametro(id = id, chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)
        every { parametroOutputPort.findById(id) } returns p
        every { parametroOutputPort.deleteById(id) } just runs

        service.delete(id)

        verify(exactly = 1) { parametroOutputPort.deleteById(id) }
    }

    @Test
    fun `delete throws BusinessException when DataIntegrityViolationException`() {
        val id = UUID.randomUUID()
        val p = Parametro(id = id, chave = "K", valorTexto = "v", tipo = "STRING", ativo = true)
        every { parametroOutputPort.findById(id) } returns p
        every { parametroOutputPort.deleteById(id) } throws
            org.springframework.dao.DataIntegrityViolationException("fk")

        assertThatThrownBy { service.delete(id) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("registros dependentes")
    }
}
