package br.com.cashflow.usecase.bootstrap_management.service

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.InvalidBootstrapSecretException
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapAdminUserData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCommand
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCongregationData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapResult
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapTenantData
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.tenant_management.port.TenantSchemaProvisionerPort
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import br.com.cashflow.usecase.user_management.port.UsuarioCommand
import br.com.cashflow.usecase.user_management.port.UsuarioCriadoResult
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class BootstrapServiceTest {
    private val tenantOutputPort: TenantOutputPort = mockk()
    private val tenantSchemaProvisioner: TenantSchemaProvisionerPort = mockk()
    private val congregationOutputPort: CongregationOutputPort = mockk()
    private val userManagementInputPort: UserManagementInputPort = mockk()

    private val configuredSecret = "valid-bootstrap-secret"
    private lateinit var service: BootstrapService
    private lateinit var selfMock: BootstrapService

    private val tenantId = UUID.randomUUID()
    private val congregationId = UUID.randomUUID()

    private fun validCommand(): BootstrapCommand =
        BootstrapCommand(
            tenant =
                BootstrapTenantData(
                    tradeName = "Church A",
                    companyName = "Church A Ltda",
                    cnpj = "12345678000190",
                    street = "Rua One",
                    number = "100",
                    complement = null,
                    neighborhood = "Centro",
                    city = "São Paulo",
                    state = "SP",
                    zipCode = "01234567",
                    phone = null,
                    email = null,
                    active = true,
                ),
            adminUser = BootstrapAdminUserData(nome = "Admin", email = "admin@example.com"),
            firstCongregation =
                BootstrapCongregationData(
                    nome = "Sede",
                    cnpj = null,
                    logradouro = "Rua One",
                    bairro = "Centro",
                    numero = "100",
                    cidade = "São Paulo",
                    uf = "SP",
                    cep = "01234567",
                    email = null,
                    telefone = null,
                    ativo = true,
                ),
        )

    @BeforeEach
    fun setUp() {
        selfMock = mockk()
        every { selfMock.saveTenantAndCommit(any()) } answers { tenantOutputPort.save(firstArg()) }
        service =
            BootstrapService(
                configuredSecret = configuredSecret,
                tenantOutputPort = tenantOutputPort,
                tenantSchemaProvisioner = tenantSchemaProvisioner,
                congregationOutputPort = congregationOutputPort,
                userManagementInputPort = userManagementInputPort,
                self = selfMock,
            )
    }

    @AfterEach
    fun tearDown() {
        io.mockk.unmockkAll()
    }

    @Test
    fun `bootstrap returns result when secret is valid and data is valid`() {
        val command = validCommand()
        val savedTenant =
            Tenant(
                id = tenantId,
                cnpj = "12345678000190",
                tradeName = "CHURCH A",
                street = "RUA ONE",
                number = "100",
                city = "SÃO PAULO",
                state = "SP",
                zipCode = "01234567",
                schemaName = "tenant_12345678000190",
            )
        val savedCongregation =
            Congregation(
                id = congregationId,
                tenantId = tenantId,
                nome = "SEDE",
                logradouro = "Rua One",
                bairro = "Centro",
                numero = "100",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        val temporaryPassword = "TempPass123!"
        val userResult =
            UsuarioCriadoResult(
                usuario =
                    AcessoListItem(
                        email = "admin@example.com",
                        nome = "Admin",
                        telefone = null,
                        tipoAcesso = "ADMIN",
                        ativo = true,
                        createdDate = null,
                        lastModifiedDate = null,
                        congregacaoId = congregationId,
                        congregacaoNome = "Sede",
                    ),
                senhaTemporaria = temporaryPassword,
            )

        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns false
        every { tenantOutputPort.save(any()) } returns savedTenant
        every { tenantSchemaProvisioner.provision(any()) } just runs
        every { congregationOutputPort.existsByCnpjExcludingId(any(), any()) } returns false
        every { congregationOutputPort.save(any()) } returns savedCongregation
        every {
            userManagementInputPort.create(
                match {
                    it.email == "admin@example.com" &&
                        it.perfil == "ADMIN" &&
                        it.congregacaoId == congregationId
                },
            )
        } returns userResult
        every { selfMock.saveCongregationAndUser(any(), any(), any()) } answers {
            congregationOutputPort.save(savedCongregation)
            val u =
                userManagementInputPort.create(
                    UsuarioCommand(
                        nome = "ADMIN",
                        email = thirdArg(),
                        telefone = null,
                        perfil = "ADMIN",
                        congregacaoId = congregationId,
                        ativo = true,
                    ),
                )
            BootstrapResult(
                tenantId = savedTenant.id!!,
                tenantSchemaName = savedTenant.schemaName,
                adminEmail = thirdArg(),
                temporaryPassword = u.senhaTemporaria,
                congregationId = savedCongregation.id!!,
            )
        }

        val result = service.bootstrap(configuredSecret, command)

        assertThat(result.tenantId).isEqualTo(tenantId)
        assertThat(result.tenantSchemaName).isEqualTo("tenant_12345678000190")
        assertThat(result.adminEmail).isEqualTo("admin@example.com")
        assertThat(result.temporaryPassword).isEqualTo(temporaryPassword)
        assertThat(result.congregationId).isEqualTo(congregationId)
        verify(exactly = 1) { tenantOutputPort.save(any()) }
        verify(exactly = 1) { tenantSchemaProvisioner.provision("tenant_12345678000190") }
        verify(exactly = 1) { congregationOutputPort.save(any()) }
        verify(exactly = 1) { userManagementInputPort.create(any()) }
    }

    @Test
    fun `bootstrap throws InvalidBootstrapSecretException when secret is blank`() {
        val command = validCommand()

        assertThatThrownBy { service.bootstrap("", command) }
            .isInstanceOf(InvalidBootstrapSecretException::class.java)
        verify(exactly = 0) { tenantOutputPort.save(any()) }
    }

    @Test
    fun `bootstrap throws InvalidBootstrapSecretException when secret is wrong`() {
        val command = validCommand()

        assertThatThrownBy { service.bootstrap("wrong-secret", command) }
            .isInstanceOf(InvalidBootstrapSecretException::class.java)
        verify(exactly = 0) { tenantOutputPort.save(any()) }
    }

    @Test
    fun `bootstrap throws ConflictException when tenant CNPJ already exists`() {
        val command = validCommand()
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns true

        assertThatThrownBy { service.bootstrap(configuredSecret, command) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ")
        verify(exactly = 0) { tenantOutputPort.save(any()) }
    }

    @Test
    fun `bootstrap throws ConflictException when admin email already exists`() {
        val command = validCommand()
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns false
        every { tenantOutputPort.save(any()) } returns
            Tenant(
                id = tenantId,
                cnpj = "12345678000190",
                schemaName = "tenant_12345678000190",
            )
        every { tenantSchemaProvisioner.provision(any()) } just runs
        every { congregationOutputPort.existsByCnpjExcludingId(any(), any()) } returns false
        every { congregationOutputPort.save(any()) } returns
            Congregation(id = congregationId, tenantId = tenantId, nome = "SEDE")
        every { userManagementInputPort.create(any()) } throws ConflictException("Email already registered")
        every { selfMock.saveCongregationAndUser(any(), any(), any()) } throws ConflictException("Email already registered")

        assertThatThrownBy { service.bootstrap(configuredSecret, command) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Email")
    }

    @Test
    fun `bootstrap with congregation cnpj set validates cnpj and checks duplicate`() {
        val command =
            validCommand().copy(
                firstCongregation =
                    BootstrapCongregationData(
                        nome = "Sede",
                        cnpj = "11.222.333/0001-81",
                        logradouro = "Rua One",
                        bairro = "Centro",
                        numero = "100",
                        cidade = "São Paulo",
                        uf = "SP",
                        cep = "01234567",
                        email = null,
                        telefone = null,
                        ativo = true,
                    ),
            )
        val savedTenant =
            Tenant(
                id = tenantId,
                cnpj = "12345678000190",
                schemaName = "tenant_12345678000190",
            )
        val savedCongregation =
            Congregation(
                id = congregationId,
                tenantId = tenantId,
                nome = "SEDE",
                cnpj = "11222333000181",
                logradouro = "Rua One",
                bairro = "Centro",
                numero = "100",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
            )
        every { tenantOutputPort.existsByCnpjExcludingId("12345678000190", null) } returns false
        every { tenantOutputPort.save(any()) } returns savedTenant
        every { tenantSchemaProvisioner.provision(any()) } just runs
        every { congregationOutputPort.existsByCnpjExcludingId("11222333000181", null) } returns false
        every { congregationOutputPort.save(any()) } returns savedCongregation
        every { userManagementInputPort.create(any()) } returns
            UsuarioCriadoResult(
                usuario = AcessoListItem("admin@example.com", "Admin", null, "ADMIN", true, null, null, congregationId, "Sede"),
                senhaTemporaria = "temp",
            )
        every { selfMock.saveTenantAndCommit(any()) } answers { tenantOutputPort.save(firstArg()) }
        every { selfMock.saveCongregationAndUser(any(), any(), any()) } answers {
            service.saveCongregationAndUser(firstArg(), secondArg(), thirdArg())
        }

        val result = service.bootstrap(configuredSecret, command)

        assertThat(result.tenantId).isEqualTo(tenantId)
        assertThat(result.congregationId).isEqualTo(congregationId)
        verify(exactly = 1) { congregationOutputPort.existsByCnpjExcludingId("11222333000181", null) }
    }
}
