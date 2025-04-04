package org.authApi.controllers;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.authApi.exceptions.TokenAndUserExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden // Hide from Swagger UI as these are implementation details
@SuppressWarnings("unused")
@ControllerAdvice
public class TokenExceptionHandler extends CustomExceptionHandler {
    
    @ExceptionHandler(TokenAndUserExceptions.MissingTokenException.class)
    public ResponseEntity<CustomErrorMessage> handleMissingTokenException(
            TokenAndUserExceptions.MissingTokenException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenAndUserExceptions.InvalidTokenException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidTokenException(
            TokenAndUserExceptions.InvalidTokenException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenAndUserExceptions.TokenExpiredException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenExpiredException(
            TokenAndUserExceptions.TokenExpiredException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(TokenAndUserExceptions.TokenAlreadyUsedException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenAlreadyUsedException(
            TokenAndUserExceptions.TokenAlreadyUsedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(TokenAndUserExceptions.TokenNotFoundForRoleException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenNotFoundForRoleException(
            TokenAndUserExceptions.TokenNotFoundForRoleException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(TokenAndUserExceptions.TokenUserMismatchException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenUserMismatchException(
            TokenAndUserExceptions.TokenUserMismatchException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(TokenAndUserExceptions.TokenVerificationFailedException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenVerificationFailedException(
            TokenAndUserExceptions.TokenVerificationFailedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
} 