package santannaf.customer.rest.entrypoint.handler

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import java.sql.SQLTransientConnectionException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import santannaf.core.exception.PeopleNotFoundException

@RestControllerAdvice
class PeopleHandlerException {
    @ExceptionHandler(
        DuplicateKeyException::class,
        IllegalArgumentException::class,
        HttpMessageNotReadableException::class,
        InvalidDefinitionException::class,
        HttpMessageConversionException::class,
        DuplicateKeyException::class,
        SQLTransientConnectionException::class
    )
    fun onBusinessException(e: Exception): ResponseEntity<*> {
        return ResponseEntity.unprocessableEntity().build<String>()
    }

    @ExceptionHandler(PeopleNotFoundException::class)
    fun onPeopleNotFoundException(e: PeopleNotFoundException): ResponseEntity<*> {
        return ResponseEntity.notFound().build<String>()
    }
}
