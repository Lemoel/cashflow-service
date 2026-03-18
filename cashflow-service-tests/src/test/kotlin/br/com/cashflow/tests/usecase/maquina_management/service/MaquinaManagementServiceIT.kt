package br.com.cashflow.tests.usecase.maquina_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import br.com.cashflow.usecase.maquina_management.port.MaquinaManagementInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@SqlSetUp(value = ["/db/scripts/maquina/load.sql"])
@SqlTearDown(value = ["/db/scripts/maquina/teardown.sql"])
class MaquinaManagementServiceIT : PostgresqlBaseTest() {
    private val congregacaoId = UUID.fromString("b2c3d4e5-f6a7-8901-2345-678901bcdef0")
    private val bancoId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef")

    @Autowired
    private lateinit var maquinaManagement: MaquinaManagementInputPort

    @Test
    fun should_CreateFindUpdateSearchListAndDelete_When_FullCrud() {
        val createRequest =
            MaquinaCreateRequestDto(
                maquinaId = "IT-MAQ-001",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
                departamentoId = null,
                ativo = true,
            )

        val created = maquinaManagement.create(createRequest)

        assertThat(created.id).isNotNull()
        assertThat(created.maquinaId).isEqualTo("IT-MAQ-001")
        assertThat(created.congregacaoId).isEqualTo(congregacaoId)
        assertThat(created.bancoId).isEqualTo(bancoId)
        assertThat(created.congregacaoNome).isNotBlank()
        assertThat(created.bancoNome).isNotBlank()

        val found = maquinaManagement.findById(created.id)
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(created.id)
        assertThat(found.maquinaId).isEqualTo("IT-MAQ-001")

        val updateRequest =
            MaquinaUpdateRequestDto(
                congregacaoId = congregacaoId,
                bancoId = bancoId,
                departamentoId = null,
                ativo = false,
            )
        val updated = maquinaManagement.update(created.id!!, updateRequest)
        assertThat(updated.ativo).isFalse()

        val searchPage = maquinaManagement.search("IT-MAQ", null, null, null, 0, 10)
        assertThat(searchPage.items).isNotEmpty
        assertThat(searchPage.items.any { it.maquinaId.contains("IT-MAQ") }).isTrue()

        val historico = maquinaManagement.listHistoricoByMaquinaId(created.id!!)
        assertThat(historico).isNotEmpty

        maquinaManagement.delete(created.id!!)
        val afterDelete = maquinaManagement.findById(created.id!!)
        assertThat(afterDelete).isNull()
    }

    @Test
    fun should_ThrowConflictException_When_CreateWithDuplicateNumeroSerie() {
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "DUP-SERIE",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
            )
        maquinaManagement.create(request)

        assertThatThrownBy { maquinaManagement.create(request) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Já existe uma máquina com este ID")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateWithBlankMaquinaId() {
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "   ",
                congregacaoId = congregacaoId,
                bancoId = bancoId,
            )

        assertThatThrownBy { maquinaManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("ID da máquina é obrigatório")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateWithCongregacaoNotFound() {
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "X",
                congregacaoId = UUID.randomUUID(),
                bancoId = bancoId,
            )

        assertThatThrownBy { maquinaManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Congregação da máquina não encontrada")
    }

    @Test
    fun should_ThrowBusinessException_When_CreateWithBancoNotFound() {
        val request =
            MaquinaCreateRequestDto(
                maquinaId = "X",
                congregacaoId = congregacaoId,
                bancoId = UUID.randomUUID(),
            )

        assertThatThrownBy { maquinaManagement.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("Banco não encontrado")
    }

    @Test
    fun should_ReturnNull_When_FindByIdNotFound() {
        val result = maquinaManagement.findById(UUID.randomUUID())
        assertThat(result).isNull()
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_UpdateNotFound() {
        val request =
            MaquinaUpdateRequestDto(
                congregacaoId = congregacaoId,
                bancoId = bancoId,
            )

        assertThatThrownBy { maquinaManagement.update(UUID.randomUUID(), request) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Máquina não encontrada")
    }

    @Test
    fun should_ThrowResourceNotFoundException_When_DeleteNotFound() {
        assertThatThrownBy { maquinaManagement.delete(UUID.randomUUID()) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Máquina não encontrada")
    }

    @Test
    fun should_ReturnPage_When_ListOrSearchWithNoFilters() {
        val page = maquinaManagement.listOrSearch(null, null, null, null, null, null, null, 0, 10)
        assertThat(page.items).isNotNull
        assertThat(page.total).isGreaterThanOrEqualTo(0)
        assertThat(page.page).isEqualTo(0)
        assertThat(page.pageSize).isEqualTo(10)
    }

    @Test
    fun should_ReturnPage_When_ListOrSearchWithMaquinaIdFilter() {
        val page = maquinaManagement.listOrSearch("NONEXISTENT", null, null, null, null, null, null, 0, 10)
        assertThat(page.items).isNotNull
        assertThat(page.total).isEqualTo(0)
    }
}
