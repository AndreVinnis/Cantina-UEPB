package com.uepb.CoreService.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CafeteriaNotFound.class,
            MenuItemNotFound.class,
            NoCafeteriaFound.class,
            NoMenuItemsYet.class
    })
    public ResponseEntity<StandardError> handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Tratamento para conflitos de dados/recursos já existentes (HTTP 409)
    @ExceptionHandler({
            EmailAlreadyExistException.class,
            MenuItemAlreadyExists.class,
            NameAlreadyExist.class
    })
    public ResponseEntity<StandardError> handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Tratamento para regras de negócio não atendidas/má requisição (HTTP 400)
    @ExceptionHandler({
            ShortPasswordException.class,
            CafeteriaIsNotActive.class
    })
    public ResponseEntity<StandardError> handleBadRequestExceptions(RuntimeException ex, HttpServletRequest request) {
        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
