package br.com.cashflow.usecase.movimento_extraction.adapter.external.controller

import br.com.cashflow.usecase.movimento_extraction.port.MovimentoExtractionInputPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/movimentos")
class MovimentoExtractionController(
    private val extractionInputPort: MovimentoExtractionInputPort,
) {
    @PostMapping("/extrair")
    fun extrair(): ResponseEntity<String> {
        extractionInputPort.extrairTodosDiasPendentes()
        return ResponseEntity.accepted().body("Extração finalizada")
    }

    @PostMapping("/lancamento-do-dia")
    fun extrairDia(
        @RequestParam data: String,
    ): ResponseEntity<String> {
        val localDate = LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE)
        extractionInputPort.extrairDia(localDate)
        return ResponseEntity.accepted().body("Extração finalizada")
    }
}
