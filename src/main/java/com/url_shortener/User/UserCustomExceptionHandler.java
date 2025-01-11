package com.url_shortener.User;

import com.url_shortener.CustomErrorMessage;
import com.url_shortener.CustomExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
public class UserCustomExceptionHandler extends CustomExceptionHandler {
    @ExceptionHandler(UserWithNoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            UserWithNoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UndefinedRoleException.class)
    public ResponseEntity<CustomErrorMessage> handleNoHashedUrlException(
            UndefinedRoleException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectRoleTokenException.class)
    public ResponseEntity<CustomErrorMessage> handleIncorrectRoleTokenException(
            IncorrectRoleTokenException e, WebRequest request) {
        return handle(e, request, HttpStatus.UNAUTHORIZED);
    }
}