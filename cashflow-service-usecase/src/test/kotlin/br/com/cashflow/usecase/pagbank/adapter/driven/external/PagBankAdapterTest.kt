package br.com.cashflow.usecase.pagbank.adapter.driven.external

import br.com.cashflow.usecase.pagbank.client.PagBankClient
import br.com.cashflow.usecase.pagbank.config.PagBankApiProperties
import br.com.cashflow.usecase.pagbank.encryption.PagBankEncryptionService
import br.com.cashflow.usecase.pagbank.port.RespostaMovimento
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate

class PagBankAdapterTest {
    private val pagBankClient: PagBankClient = mockk()
    private val pagBankEncryptionService: PagBankEncryptionService = mockk()
    private val properties =
        PagBankApiProperties().apply {
            pageSize = 500
            inicio = "2025-01-01"
        }

    private val adapter =
        PagBankAdapter(
            pagBankClient = pagBankClient,
            pagBankEncryptionService = pagBankEncryptionService,
            objectMapper = jacksonObjectMapper(),
            pagBankApiProperties = properties,
        )

    @Test
    fun `buscarMovimentos retorna nao validada quando header validado nao e true`() {
        val headers = HttpHeaders().apply { set("validado", "false") }
        every { pagBankClient.getMovimentos("2025-01-15", 1, 500) } returns
            ResponseEntity("{}", headers, HttpStatus.OK)

        val resultado = adapter.buscarMovimentos(LocalDate.of(2025, 1, 15), 1)

        assertThat(resultado).isEqualTo(RespostaMovimento.NaoValidada)
    }

    @Test
    fun `buscarMovimentos retorna sucesso com payload criptografado`() {
        val json =
            """
            {
              "detalhes": [{"codigo_transacao":"TX1","numero_serie_leitor":"S123"}],
              "pagination": {"total_pages": 2, "page": 1, "total_elements": 10}
            }
            """.trimIndent()
        val headers = HttpHeaders().apply { set("validado", "true") }
        every { pagBankClient.getMovimentos("2025-01-16", 1, 500) } returns
            ResponseEntity(json, headers, HttpStatus.OK)
        every { pagBankEncryptionService.encrypt(json) } returns "payload-criptografado"

        val resultado = adapter.buscarMovimentos(LocalDate.of(2025, 1, 16), 1)

        assertThat(resultado).isInstanceOf(RespostaMovimento.Sucesso::class.java)
        val sucesso = resultado as RespostaMovimento.Sucesso
        assertThat(sucesso.payloadCriptografado).isEqualTo("payload-criptografado")
        assertThat(sucesso.totalPaginas).isEqualTo(2)
        assertThat(sucesso.totalElementos).isEqualTo(10)
        assertThat(sucesso.detalhes).hasSize(1)
    }

    @Test
    fun `buscarMovimentos retorna erro de desserializacao para json invalido`() {
        val headers = HttpHeaders().apply { set("validado", "true") }
        every { pagBankClient.getMovimentos("2025-01-17", 1, 500) } returns
            ResponseEntity("{", headers, HttpStatus.OK)

        val resultado = adapter.buscarMovimentos(LocalDate.of(2025, 1, 17), 1)

        assertThat(resultado).isInstanceOf(RespostaMovimento.ErroDesserializacao::class.java)
    }
}
