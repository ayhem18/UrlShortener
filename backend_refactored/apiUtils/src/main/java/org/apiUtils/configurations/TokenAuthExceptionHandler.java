package org.apiUtils.configurations;

import org.apiUtils.commonClasses.CustomExceptionHandler;
import org.apiUtils.commonClasses.TokenAuthController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;


@ControllerAdvice
public class TokenAuthExceptionHandler extends CustomExceptionHandler {
    @ExceptionHandler(TokenAuthController.TokenNotFoundException.class)
    public ResponseEntity<CustomErrorMessage> handleTokenNotFoundException(
            TokenAuthController.TokenNotFoundException e, WebRequest request) {
        return handle(e, request, HttpStatus.FORBIDDEN); // authenticated user but not authorized
    }

}