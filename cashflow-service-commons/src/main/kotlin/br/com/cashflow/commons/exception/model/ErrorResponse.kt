package br.com.cashflow.commons.exception.model

data class FieldError(
    val field: String,
    val message: String,
)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val message: String,
    val path: String,
    val details: List<FieldError>? = null,
    val error: String? = null,
)
