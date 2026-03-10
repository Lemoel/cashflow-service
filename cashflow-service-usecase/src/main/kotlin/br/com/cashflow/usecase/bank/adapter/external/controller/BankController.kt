package br.com.cashflow.usecase.bank.adapter.external.controller

import br.com.cashflow.usecase.bank.port.BankOutputPort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class BankOptionResponse(
    val id: String,
    val nome: String?,
)

@RestController
@RequestMapping("/api/v1/bancos")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_MATRIZ')")
class BankController(
    private val bankOutputPort: BankOutputPort,
) {
    @GetMapping
    fun list(): List<BankOptionResponse> =
        bankOutputPort.findAllOrderByNomeAsc().map { b ->
            BankOptionResponse(
                id = b.id!!.toString(),
                nome = b.nome,
            )
        }
}
