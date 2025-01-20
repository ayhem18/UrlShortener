package org.api.controllers.user;

import org.api.controllers.CustomExceptionHandler;
import org.common.RoleManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;


@ControllerAdvice
public class UserCustomExceptionHandler extends CustomExceptionHandler {
    @ExceptionHandler(UserWithNoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            UserWithNoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RoleManager.NoExistingRoleException.class)
    public ResponseEntity<CustomErrorMessage> handleNoHashedUrlException(
            RoleManager.NoExistingRoleException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectRoleTokenException.class)
    public ResponseEntity<CustomErrorMessage> handleIncorrectRoleTokenException(
            IncorrectRoleTokenException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserBeforeOwnerException.class)
    public ResponseEntity<CustomErrorMessage> handleUserBeforeOwnerException(
            UserBeforeOwnerException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AlreadyExistingUserException.class)
    public ResponseEntity<CustomErrorMessage> handleAlreadyExistingUserException(
            AlreadyExistingUserException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN);
    }

}