package br.com.cashflow.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@SpringBootApplication(scanBasePackages = ["br.com.cashflow"])
@EnableJdbcRepositories(basePackages = ["br.com.cashflow.usecase"])
@EnableJdbcAuditing(auditorAwareRef = "auditorProvider")
class CashflowApplication

fun main(args: Array<String>) {
    runApplication<CashflowApplication>(*args)
}
