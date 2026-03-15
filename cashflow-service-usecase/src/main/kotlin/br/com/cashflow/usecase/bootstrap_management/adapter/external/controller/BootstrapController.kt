package br.com.cashflow.usecase.bootstrap_management.adapter.external.controller

import br.com.cashflow.usecase.bootstrap_management.adapter.external.dto.BootstrapRequestDto
import br.com.cashflow.usecase.bootstrap_management.adapter.external.dto.BootstrapResponseDto
import br.com.cashflow.usecase.bootstrap_management.adapter.external.dto.toBootstrapCommand
import br.com.cashflow.usecase.bootstrap_management.adapter.external.dto.toResponseDto
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapInputPort
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1/bootstrap")
class BootstrapController(
    private val bootstrapInputPort: BootstrapInputPort,
) {
    @PostMapping
    fun bootstrap(
        @RequestHeader("X-Bootstrap-Secret") secret: String?,
        @Valid @RequestBody request: BootstrapRequestDto,
    ): ResponseEntity<BootstrapResponseDto> {
        val effectiveSecret = secret ?: ""
        val command = request.toBootstrapCommand()
        val result = bootstrapInputPort.bootstrap(effectiveSecret, command)
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val responseDto = result.toResponseDto(baseUrl)
        val location = "/api/v1/tenants/${result.tenantId}"
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", location)
            .body(responseDto)
    }
}
