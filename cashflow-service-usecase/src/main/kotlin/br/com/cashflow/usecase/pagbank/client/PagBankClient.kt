package br.com.cashflow.usecase.pagbank.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "pagBankClient",
    url = "\${pagbank.api.url}",
    configuration = [PagBankFeignConfig::class],
)
interface PagBankClient {
    @GetMapping("/movement/v3.00/transactional/{data}")
    fun getMovimentos(
        @PathVariable("data") data: String,
        @RequestParam("pageNumber") pageNumber: Int,
        @RequestParam("pageSize") pageSize: Int,
    ): ResponseEntity<String>
}
