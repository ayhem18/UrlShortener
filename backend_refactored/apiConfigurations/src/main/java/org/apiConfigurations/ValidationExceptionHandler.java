package org.apiConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ControllerAdvice
public class ValidationExceptionHandler extends CustomExceptionHandler{

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorMessage> handleValidationExceptions(
            MethodArgumentNotValidException e, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // serialize the errors to a json string
        try {
            String jsonErrors = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(errors);
            return super.handle(HttpStatus.BAD_REQUEST, jsonErrors, request.getDescription(false));
        } catch (JsonProcessingException exp) {
            return super.handle(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request.getDescription(false));
        }
    }
}
