package com.sanosysalvos.authservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsBadRequest() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(
                new RuntimeException("boom"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("boom", response.getBody().get("error"));
    }

    @Test
    void handleBadCredentialsException_returnsUnauthorized() {
        ResponseEntity<Map<String, String>> response = handler.handleBadCredentialsException(
                new BadCredentialsException("bad"));

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid username or password", response.getBody().get("error"));
    }

    @Test
    void handleValidationExceptions_returnsFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "Required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Required", response.getBody().get("username"));
    }

    @Test
    void handleGeneralException_returnsInternalServerError() {
        ResponseEntity<Map<String, String>> response = handler.handleGeneralException(new Exception("boom"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
