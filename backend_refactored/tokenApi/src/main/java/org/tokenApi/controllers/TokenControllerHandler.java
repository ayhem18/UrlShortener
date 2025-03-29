package org.tokenApi.controllers;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.tokenApi.exceptions.TokenExceptions;
import org.utils.CustomErrorMessage;
import io.swagger.v3.oas.annotations.Hidden;

import java.time.LocalDateTime;

@Hidden // Hide from Swagger UI as these are implementation details
@ControllerAdvice
@SuppressWarnings(value = "unused")
public class TokenControllerHandler extends CustomExceptionHandler {
    
    @ExceptionHandler(TokenExceptions.InsufficientRoleAuthority.class)
    public ResponseEntity<CustomErrorMessage> handleInsufficientRoleException(
            TokenExceptions.InsufficientRoleAuthority ex, WebRequest request) {
        return new ResponseEntity<>(
            new CustomErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)),
            HttpStatus.FORBIDDEN);
    }
    

    @ExceptionHandler(TokenExceptions.NumTokensLimitExceeded.class)
    public ResponseEntity<CustomErrorMessage> handleTokenLimitExceededException(
            TokenExceptions.NumTokensLimitExceeded ex, WebRequest request) {
        return new ResponseEntity<>(
            new CustomErrorMessage(
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)),
            HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(TokenExceptions.TokenGenerationException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenGenerationException(
            TokenExceptions.TokenGenerationException ex, WebRequest request) {
        return new ResponseEntity<>(
            new CustomErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(TokenExceptions.NoActiveTokenException.class)
    public ResponseEntity<CustomErrorMessage> handleUserNotFoundException(
            TokenExceptions.NoActiveTokenException ex, WebRequest request) {
        return new ResponseEntity<>(
            new CustomErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExceptions.ActiveTokenNotFoundException.class)
    public ResponseEntity<CustomErrorMessage> handleActiveTokenNotFoundException(
            TokenExceptions.ActiveTokenNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(
            new CustomErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)),
            HttpStatus.NOT_FOUND);
    }
}