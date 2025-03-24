package org.urlService.controllers;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.urlService.exceptions.UrlExceptions;
import org.utils.CustomErrorMessage;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden // Hide from Swagger UI as these are implementation details
@ControllerAdvice
@SuppressWarnings(value = "unused")
public class UrlControllerHandler extends CustomExceptionHandler {
    
    @ExceptionHandler(UrlExceptions.InvalidUrlException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            UrlExceptions.InvalidUrlException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UrlExceptions.DailyLimitExceededException.class)
    public ResponseEntity<CustomErrorMessage> handleDailyLimitExceededException(
            UrlExceptions.DailyLimitExceededException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UrlExceptions.UrlCompanyDomainExpired.class)
    public ResponseEntity<CustomErrorMessage> handleUrlCompanyDomainExpired(
            UrlExceptions.UrlCompanyDomainExpired e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UrlExceptions.InvalidTopLevelDomainException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidTopLevelDomainException(
            UrlExceptions.InvalidTopLevelDomainException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UrlExceptions.UrlCompanyDomainMistachException.class)
    public ResponseEntity<CustomErrorMessage> handleNoCompanyUrlDataFound(
            UrlExceptions.UrlCompanyDomainMistachException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}