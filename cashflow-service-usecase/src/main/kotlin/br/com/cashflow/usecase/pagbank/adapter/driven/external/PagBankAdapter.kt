package br.com.cashflow.usecase.pagbank.adapter.driven.external

import br.com.cashflow.usecase.pagbank.client.MovimentoApiResponse
import br.com.cashflow.usecase.pagbank.client.PagBankClient
import br.com.cashflow.usecase.pagbank.config.PagBankApiProperties
import br.com.cashflow.usecase.pagbank.encryption.PagBankEncryptionService
import br.com.cashflow.usecase.pagbank.port.PagBankOutputPort
import br.com.cashflow.usecase.pagbank.port.RespostaMovimento
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class PagBankAdapter(
    private val pagBankClient: PagBankClient,
    private val pagBankEncryptionService: PagBankEncryptionService,
    private val objectMapper: ObjectMapper,
    private val pagBankApiProperties: PagBankApiProperties,
) : PagBankOutputPort {
    override fun buscarMovimentos(
        data: LocalDate,
        pagina: Int,
    ): RespostaMovimento {
        val dataStr = data.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val response = pagBankClient.getMovimentos(dataStr, pagina, pagBankApiProperties.pageSize)

        val validado = response.headers["validado"]?.firstOrNull()?.lowercase() == "true"
        if (!validado) {
            return RespostaMovimento.NaoValidada
        }

        val body = response.body ?: return RespostaMovimento.SemConteudo

        val movimentoApiResponse =
            try {
                objectMapper.readValue<MovimentoApiResponse>(body)
            } catch (error: Exception) {
                log.error("Erro ao deserializar JSON para data={} pagina={}", data, pagina, error)
                return RespostaMovimento.ErroDesserializacao(error.message ?: "Erro desconhecido")
            }

        val payloadCriptografado = pagBankEncryptionService.encrypt(body)
        val pagination = movimentoApiResponse.pagination

        return RespostaMovimento.Sucesso(
            detalhes = movimentoApiResponse.detalhes,
            payloadCriptografado = payloadCriptografado,
            totalPaginas = pagination?.totalPages ?: 1,
            totalElementos = pagination?.totalElements ?: movimentoApiResponse.detalhes.size,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(PagBankAdapter::class.java)
    }
}
