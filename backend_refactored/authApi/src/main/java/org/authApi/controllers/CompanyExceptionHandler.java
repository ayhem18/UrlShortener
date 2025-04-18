package org.authApi.controllers;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.authApi.exceptions.CompanyAndUserExceptions;
import org.authApi.exceptions.CompanyExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.InvalidUrlException;
import org.utils.CustomErrorMessage;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden // Hide from Swagger UI as these are implementation details
@SuppressWarnings("unused")
@ControllerAdvice
public class CompanyExceptionHandler extends CustomExceptionHandler {
    // Company Exceptions
    @ExceptionHandler(CompanyExceptions.ExistingCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleExistingCompanyException(
            CompanyExceptions.ExistingCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyExceptions.ExistingTopLevelDomainException.class)
    public ResponseEntity<CustomErrorMessage> handleExistingTopLevelDomainException(
            CompanyExceptions.ExistingTopLevelDomainException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyExceptions.NoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleNoCompanyException(
            CompanyExceptions.NoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyExceptions.CompanyAlreadyVerifiedException.class)
    public ResponseEntity<CustomErrorMessage> handleCompanyAlreadyVerifiedException(
            CompanyExceptions.CompanyAlreadyVerifiedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    // Company and User Exceptions (shared between controllers, handled here)
    @ExceptionHandler(CompanyAndUserExceptions.UserCompanyMisalignedException.class)
    public ResponseEntity<CustomErrorMessage> handleUserCompanyMisalignedException(
            CompanyAndUserExceptions.UserCompanyMisalignedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyAndUserExceptions.MultipleOwnersException.class)
    public ResponseEntity<CustomErrorMessage> handleMultipleOwnersException(
            CompanyAndUserExceptions.MultipleOwnersException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            CompanyAndUserExceptions.MultipleOwnersException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}
