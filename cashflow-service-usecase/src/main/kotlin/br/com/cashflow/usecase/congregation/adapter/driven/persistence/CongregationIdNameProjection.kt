package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import java.util.UUID

interface CongregationIdNameProjection {
    fun getId(): UUID

    fun getNome(): String
}
