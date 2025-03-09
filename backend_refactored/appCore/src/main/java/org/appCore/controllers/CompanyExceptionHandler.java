package org.appCore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;
import org.appCore.exceptions.CompanyExceptions;

@ControllerAdvice
public class CompanyExceptionHandler extends CustomExceptionHandler {

    @ExceptionHandler(CompanyExceptions.ExistingCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleExistingCompanyException(
            CompanyExceptions.ExistingCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyExceptions.NoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleNoCompanyException(
            CompanyExceptions.NoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }
}
