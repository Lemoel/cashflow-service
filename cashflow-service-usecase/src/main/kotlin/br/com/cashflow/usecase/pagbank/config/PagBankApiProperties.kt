package br.com.cashflow.usecase.pagbank.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "pagbank.api")
class PagBankApiProperties {
    var pageSize: Int = 1000
    var inicio: String = "2025-01-01"
}
