package br.com.cashflow.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["br.com.cashflow"])
@EntityScan(basePackages = ["br.com.cashflow.usecase"])
@EnableJpaRepositories(basePackages = ["br.com.cashflow.usecase"])
@EnableFeignClients(basePackages = ["br.com.cashflow"])
class CashflowApplication

fun main(args: Array<String>) {
    runApplication<CashflowApplication>(*args)
}
