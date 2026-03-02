package br.com.cashflow.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing

@SpringBootApplication(scanBasePackages = ["br.com.cashflow"])
@EnableJdbcAuditing(auditorAwareRef = "auditorProvider")
class CashflowApplication

fun main(args: Array<String>) {
    runApplication<CashflowApplication>(*args)
}
