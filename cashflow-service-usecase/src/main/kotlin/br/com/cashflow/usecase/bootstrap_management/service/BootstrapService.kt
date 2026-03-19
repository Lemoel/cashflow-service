package br.com.cashflow.usecase.bootstrap_management.service

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.InvalidBootstrapSecretException
import br.com.cashflow.commons.tenant.TenantContext
import br.com.cashflow.commons.util.CnpjValidator
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCommand
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapInputPort
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapResult
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.tenant_management.port.TenantSchemaProvisionerPort
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import br.com.cashflow.usecase.user_management.port.UsuarioCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private const val CNPJ_DIGITS_LENGTH = 14

@Service
class BootstrapService(
    @Value("\${app.bootstrap.secret:}")
    private val configuredSecret: String,
    private val tenantOutputPort: TenantOutputPort,
    private val tenantSchemaProvisioner: TenantSchemaProvisionerPort,
    private val congregationOutputPort: CongregationOutputPort,
    private val userManagementInputPort: UserManagementInputPort,
    @Lazy private val self: BootstrapService,
) : BootstrapInputPort {
    @Transactional
    override fun bootstrap(
        secret: String,
        command: BootstrapCommand,
    ): BootstrapResult {
        if (secret.isBlank() || secret != configuredSecret) {
            throw InvalidBootstrapSecretException()
        }

        try {
            val adminEmail =
                command.adminUser.email
                    .trim()
                    .lowercase()
            val tenant = buildTenant(command)
            requireCnpjLength(tenant.cnpj)

            if (tenantOutputPort.existsByCnpjExcludingId(tenant.cnpj, null)) {
                throw ConflictException("CNPJ already registered")
            }

            val savedTenant = self.saveTenantAndCommit(tenant)
            tenantSchemaProvisioner.provision(savedTenant.schemaName)
            TenantContext.setSchema(savedTenant.schemaName)

            return self.saveCongregationAndUser(command, savedTenant, adminEmail)
        } finally {
            TenantContext.clear()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveTenantAndCommit(tenant: Tenant): Tenant = tenantOutputPort.save(tenant)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveCongregationAndUser(
        command: BootstrapCommand,
        savedTenant: Tenant,
        adminEmail: String,
    ): BootstrapResult {
        val congregation = buildCongregation(command, savedTenant.id!!)
        validateCongregationRequiredFields(congregation)
        val cnpjDigits = congregation.cnpj

        if (!cnpjDigits.isNullOrBlank()) {
            requireValidCnpjDigits(cnpjDigits)
            if (congregationOutputPort.existsByCnpjExcludingId(cnpjDigits, null)) {
                throw ConflictException("Já existe uma congregação com este CNPJ")
            }
        }

        val savedCongregation = congregationOutputPort.save(congregation)
        val userResult =
            userManagementInputPort.create(
                UsuarioCommand(
                    nome =
                        command.adminUser.nome
                            .trim()
                            .uppercase(),
                    email = adminEmail,
                    telefone = null,
                    perfil = "ADMIN",
                    congregacaoId = savedCongregation.id!!,
                    ativo = true,
                ),
            )

        return BootstrapResult(
            tenantId = savedTenant.id!!,
            tenantSchemaName = savedTenant.schemaName,
            adminEmail = adminEmail,
            temporaryPassword = userResult.senhaTemporaria,
            congregationId = savedCongregation.id!!,
        )
    }

    private fun buildTenant(command: BootstrapCommand): Tenant {
        val t = command.tenant
        val digitsOnly = t.cnpj.filter { it.isDigit() }
        return Tenant(
            cnpj = digitsOnly,
            tradeName = t.tradeName.trim().uppercase(),
            companyName = t.companyName?.trim()?.uppercase(),
            street = t.street.trim().uppercase(),
            number = t.number.trim(),
            complement = t.complement?.trim()?.uppercase(),
            neighborhood = t.neighborhood?.trim()?.uppercase(),
            city = t.city.trim().uppercase(),
            state = t.state.trim().uppercase(),
            zipCode = t.zipCode.trim(),
            phone = t.phone?.trim(),
            email = t.email?.trim()?.lowercase(),
            active = t.active,
            schemaName = "tenant_$digitsOnly",
        )
    }

    private fun buildCongregation(
        command: BootstrapCommand,
        tenantId: UUID,
    ): Congregation {
        val c = command.firstCongregation
        val cnpjDigits = c.cnpj?.let { CnpjValidator.clean(it).takeIf { d -> d.isNotBlank() } }
        return Congregation(
            tenantId = tenantId,
            setorialId = null,
            nome = c.nome.trim().uppercase(),
            cnpj = cnpjDigits,
            logradouro = c.logradouro.trim(),
            bairro = c.bairro.trim().uppercase(),
            numero = c.numero.trim(),
            cidade = c.cidade.trim().uppercase(),
            uf = c.uf.trim().uppercase(),
            cep = c.cep.trim(),
            email = c.email?.trim()?.lowercase(),
            telefone = c.telefone?.trim(),
            ativo = c.ativo,
        )
    }

    private fun requireCnpjLength(cnpj: String) {
        if (cnpj.length != CNPJ_DIGITS_LENGTH) {
            throw IllegalArgumentException("cnpj must contain exactly $CNPJ_DIGITS_LENGTH digits")
        }
    }

    private fun validateCongregationRequiredFields(c: Congregation) {
        if (c.nome.isBlank()) throw IllegalArgumentException("O nome é obrigatório")
        if (c.logradouro.isBlank()) throw IllegalArgumentException("O logradouro é obrigatório")
        if (c.bairro.isBlank()) throw IllegalArgumentException("O bairro é obrigatório")
        if (c.numero.isBlank()) throw IllegalArgumentException("O número é obrigatório")
        if (c.cidade.isBlank()) throw IllegalArgumentException("A cidade é obrigatória")
        if (c.uf.isBlank()) throw IllegalArgumentException("A UF é obrigatória")
        if (c.cep.isBlank()) throw IllegalArgumentException("O CEP é obrigatório")
    }

    private fun requireValidCnpjDigits(cnpj: String): String {
        val digits = CnpjValidator.clean(cnpj)
        if (digits.isBlank()) {
            throw IllegalArgumentException("CNPJ é inválido.")
        }
        if (digits.length != CNPJ_DIGITS_LENGTH) {
            throw IllegalArgumentException("CNPJ deve ter exatamente 14 dígitos numéricos")
        }
        if (!CnpjValidator.isValid(digits)) {
            throw IllegalArgumentException("CNPJ inválido: dígitos verificadores não conferem")
        }
        return digits
    }
}
