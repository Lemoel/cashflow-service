package br.com.cashflow.usecase.bootstrap_management.adapter.external.dto

import br.com.cashflow.usecase.bootstrap_management.port.BootstrapAdminUserData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCommand
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCongregationData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapTenantData
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequestDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class BootstrapRequestDto(
    @field:NotNull(message = "Dados do tenant são obrigatórios")
    @field:Valid
    val tenant: TenantCreateRequestDto,

    @field:NotNull(message = "Dados do usuário administrador são obrigatórios")
    @field:Valid
    val adminUser: BootstrapAdminUserDto,

    @field:NotNull(message = "Dados da primeira congregação são obrigatórios")
    @field:Valid
    val firstCongregation: BootstrapCongregationDto,
)

fun BootstrapRequestDto.toBootstrapCommand(): BootstrapCommand =
    BootstrapCommand(
        tenant =
            BootstrapTenantData(
                tradeName = tenant.tradeName,
                companyName = tenant.companyName,
                cnpj = tenant.cnpj,
                street = tenant.street,
                number = tenant.number,
                complement = tenant.complement,
                neighborhood = tenant.neighborhood,
                city = tenant.city,
                state = tenant.state,
                zipCode = tenant.zipCode,
                phone = tenant.phone,
                email = tenant.email,
                active = tenant.active,
            ),
        adminUser =
            BootstrapAdminUserData(
                nome = adminUser.nome,
                email = adminUser.email,
            ),
        firstCongregation =
            BootstrapCongregationData(
                nome = firstCongregation.nome,
                cnpj = firstCongregation.cnpj,
                logradouro = firstCongregation.logradouro,
                bairro = firstCongregation.bairro,
                numero = firstCongregation.numero,
                cidade = firstCongregation.cidade,
                uf = firstCongregation.uf,
                cep = firstCongregation.cep,
                email = firstCongregation.email,
                telefone = firstCongregation.telefone,
                ativo = firstCongregation.ativo,
            ),
    )
