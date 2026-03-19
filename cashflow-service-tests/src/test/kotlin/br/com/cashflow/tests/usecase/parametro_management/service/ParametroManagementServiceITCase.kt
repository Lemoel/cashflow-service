package br.com.cashflow.tests.usecase.parametro_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.EnumTipoParametro
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequestDto
import br.com.cashflow.usecase.parametro_management.port.ParametroManagementInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID

@SqlTearDown(value = ["/db/scripts/parametro/teardown.sql"])
class ParametroManagementServiceITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var parametroManagement: ParametroManagementInputPort

    @Test
    fun should_CreateFindUpdateFindAllFindChavesAndDelete_When_FullCrud() {
        val createRequest =
            ParametroCreateRequestDto(
                chave = "CHAVE_CRUD",
                valor = "valor texto",
                tipo = EnumTipoParametro.TEXTO,
                ativo = true,
            )

        val created = parametroManagement.create(createRequest)

        assertThat(created.id).isNotNull()
        assertThat(created.chave).isEqualTo("CHAVE_CRUD")
        assertThat(created.valorTexto).isEqualTo("valor texto")
        assertThat(created.tipo).isEqualTo("STRING")
        assertThat(created.ativo).isTrue()

        val found = parametroManagement.findById(created.id!!)
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.chave).isEqualTo("CHAVE_CRUD")

        val updateRequest =
            ParametroUpdateRequestDto(
                chave = "CHAVE_CRUD_ATUALIZADA",
                valor = "novo valor",
                tipo = EnumTipoParametro.TEXTO,
                ativo = false,
            )
        val updated = parametroManagement.update(created.id!!, updateRequest)
        assertThat(updated.chave).isEqualTo("CHAVE_CRUD_ATUALIZADA")
        assertThat(updated.valorTexto).isEqualTo("novo valor")
        assertThat(updated.ativo).isFalse()

        val page = parametroManagement.findAll(null, 0, 10)
        assertThat(page.items).isNotEmpty
        assertThat(page.total).isGreaterThanOrEqualTo(1)

        val chaves = parametroManagement.findChavesForDropdown()
        assertThat(chaves.any { it.first == "CHAVE_CRUD_ATUALIZADA" }).isTrue()

        parametroManagement.delete(created.id!!)
        val afterDelete = parametroManagement.findById(created.id!!)
        assertThat(afterDelete).isNull()
    }

    @Test
    fun should_ThrowConflictException_When_CreateWithDuplicateChave() {
        val request =
            ParametroCreateRequestDto(
                chave = "CHAVE_DUP",
                valor = "v",
                tipo = EnumTipoParametro.TEXTO,
            )
        parametroManagement.create(request)

        assertThatThrownBy { parametroManagement.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe um parâmetro com esta chave")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateWithBlankChave() {
        val request =
            ParametroCreateRequestDto(
                chave = "  ",
                valor = "v",
                tipo = EnumTipoParametro.TEXTO,
            )

        assertThatThrownBy { parametroManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("chave é obrigatória")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateTipoInteiroAndValorNotNumeric() {
        val request =
            ParametroCreateRequestDto(
                chave = "K_INT",
                valor = "abc",
                tipo = EnumTipoParametro.INTEIRO,
            )

        assertThatThrownBy { parametroManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("numérico")
    }

    @Test
    fun should_CreateAndFind_When_TipoDecimalWithNumericValor() {
        val request =
            ParametroCreateRequestDto(
                chave = "K_DECIMAL",
                valor = "10.5",
                tipo = EnumTipoParametro.DECIMAL,
                ativo = true,
            )
        val created = parametroManagement.create(request)
        assertThat(created.valorDecimal).isEqualByComparingTo(BigDecimal("10.5"))
        assertThat(created.tipo).isEqualTo("DOUBLE")

        val found = parametroManagement.findById(created.id!!)
        assertThat(found!!.valorDecimal).isEqualByComparingTo(BigDecimal("10.5"))
    }

    @Test
    fun should_ReturnNull_When_FindByIdNotFound() {
        val result = parametroManagement.findById(UUID.randomUUID())
        assertThat(result).isNull()
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_UpdateNotFound() {
        val request =
            ParametroUpdateRequestDto(
                chave = "K",
                valor = "v",
                tipo = EnumTipoParametro.TEXTO,
            )

        assertThatThrownBy { parametroManagement.update(UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Parâmetro não encontrado")
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_DeleteNotFound() {
        assertThatThrownBy { parametroManagement.delete(UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Parâmetro não encontrado")
    }

    @Test
    fun should_ReturnMatchingItems_When_FindAllWithChaveFilter() {
        parametroManagement.create(
            ParametroCreateRequestDto(
                chave = "FILTRO_A",
                valor = "1",
                tipo = EnumTipoParametro.TEXTO,
            ),
        )
        parametroManagement.create(
            ParametroCreateRequestDto(
                chave = "FILTRO_B",
                valor = "2",
                tipo = EnumTipoParametro.TEXTO,
            ),
        )

        val page =
            parametroManagement.findAll(
                ParametroFilterModel(chave = "FILTRO_A", ativo = null),
                0,
                10,
            )

        assertThat(page.items).isNotEmpty
        assertThat(page.items.all { it.chave.contains("FILTRO_A") }).isTrue()
    }
}
