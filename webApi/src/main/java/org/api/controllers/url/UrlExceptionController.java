package org.api.controllers.url;

import org.api.controllers.CustomExceptionHandler;
import org.common.SubscriptionManager;
import org.example.UrlValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;


@ControllerAdvice
public class UrlExceptionController extends CustomExceptionHandler {
    @ExceptionHandler(SubscriptionManager.SubscriptionViolatedException.class)
    public ResponseEntity<CustomErrorMessage> handleSubscriptionViolatedException(
            SubscriptionManager.SubscriptionViolatedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UrlValidator.InvalidUrlException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            UrlValidator.InvalidUrlException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}
