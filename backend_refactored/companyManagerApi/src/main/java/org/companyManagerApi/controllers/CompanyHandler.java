package org.companyManagerApi.controllers;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.companyManagerApi.exceptions.CompanyMngExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@SuppressWarnings("unused")
@ControllerAdvice
public class CompanyHandler extends CustomExceptionHandler {

    @ExceptionHandler(CompanyMngExceptions.SameSubscriptionInUpdateException.class)
    public ResponseEntity<CustomErrorMessage> handleSameSubscriptionInUpdateException(
            CompanyMngExceptions.SameSubscriptionInUpdateException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    
}