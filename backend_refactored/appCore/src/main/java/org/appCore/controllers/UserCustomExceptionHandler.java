package org.appCore.controllers;

import org.appCore.exceptions.CompanyAndUserExceptions;
import org.appCore.exceptions.UserExceptions;
import org.access.RoleManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;


@ControllerAdvice
public class UserCustomExceptionHandler extends CustomExceptionHandler {
    @ExceptionHandler(UserExceptions.UserWithNoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            UserExceptions.UserWithNoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RoleManager.NoExistingRoleException.class)
    public ResponseEntity<CustomErrorMessage> handleNoHashedUrlException(
            RoleManager.NoExistingRoleException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyAndUserExceptions.UserBeforeOwnerException.class)
    public ResponseEntity<CustomErrorMessage> handleUserBeforeOwnerException(
            CompanyAndUserExceptions.UserBeforeOwnerException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserExceptions.AlreadyExistingUserException.class)
    public ResponseEntity<CustomErrorMessage> handleAlreadyExistingUserException(
            UserExceptions.AlreadyExistingUserException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN);
    }
}