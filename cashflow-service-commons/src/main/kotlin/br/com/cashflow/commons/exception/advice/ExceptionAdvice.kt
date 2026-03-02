package br.com.cashflow.commons.exception.advice

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.commons.exception.model.ErrorResponse
import br.com.cashflow.commons.exception.model.FieldError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class ExceptionAdvice {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        exception: ResourceNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.NOT_FOUND.value(),
                message = exception.message ?: "Resource not found",
                path = request.requestURI,
                error = "Not Found",
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        exception: BusinessException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
                message = exception.message ?: "Business error",
                path = request.requestURI,
                error = "Unprocessable Entity",
            )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        exception: AuthenticationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.UNAUTHORIZED.value(),
                message = exception.message ?: "Unauthorized",
                path = request.requestURI,
                error = "Unauthorized",
            )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        exception: AccessDeniedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.FORBIDDEN.value(),
                message = exception.message ?: "Access denied",
                path = request.requestURI,
                error = "Forbidden",
            )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.BAD_REQUEST.value(),
                message = exception.message ?: "Invalid argument",
                path = request.requestURI,
                error = "Bad Request",
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val details =
            exception.bindingResult.fieldErrors.map { err ->
                FieldError(
                    field = err.field,
                    message = err.defaultMessage ?: "Invalid value",
                )
            }
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Validation failed",
                path = request.requestURI,
                details = details,
                error = "Bad Request",
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = exception.message ?: "Internal server error",
                path = request.requestURI,
                error = "Internal Server Error",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
