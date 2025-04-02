package org.apiUtils.configurations;

import java.util.HashMap;
import java.util.Map;

import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.CommonExceptions;
import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.apiUtils.commonClasses.TokenAuthController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonExceptionsHandler extends CustomExceptionHandler {

    @ExceptionHandler(TokenAuthController.TokenNotFoundException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenNotFoundException(
            TokenAuthController.TokenNotFoundException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN); // authenticated user but not authorized
    }

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
    

    // this exception can be thrown by at least 2 controllers: authController and companyController
    @ExceptionHandler(SubscriptionManager.NoExistingSubscription.class)
    public ResponseEntity<CustomErrorMessage> handleNoExistingSubscription(
            SubscriptionManager.NoExistingSubscription e, WebRequest request) {
        return super.handle(HttpStatus.BAD_REQUEST, e.getMessage(), request.getDescription(false));
    }


    // this exception can be thrown by at least 2 controllers: TokenController and CompanyController
    @ExceptionHandler(CommonExceptions.InsufficientRoleAuthority.class)
    public ResponseEntity<CustomErrorMessage> handleInsufficientRoleAuthority(
            CommonExceptions.InsufficientRoleAuthority e, WebRequest request) {
        return super.handle(HttpStatus.FORBIDDEN, e.getMessage(), request.getDescription(false));
    }
    

    // this exception can be thrown by at least 2 controllers: TokenController and CompanyController
    @ExceptionHandler(CommonExceptions.UserNotFoundException.class)
    public ResponseEntity<CustomErrorMessage> handleUserNotFoundException(
            CommonExceptions.UserNotFoundException e, WebRequest request) {
        return super.handle(HttpStatus.NOT_FOUND, e.getMessage(), request.getDescription(false));
    }
    
}
