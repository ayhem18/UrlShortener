package com.url_shortener.User;

import com.url_shortener.CustomErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;


@ControllerAdvice
public class UserExceptionHandler extends ResponseEntityExceptionHandler {
    private ResponseEntity<CustomErrorMessage> handle(
            RuntimeException e, WebRequest request, HttpStatus s) {
        CustomErrorMessage body = new CustomErrorMessage(
                s.value(),
                LocalDateTime.now(),
                e.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(body, s);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidUrlException(
            InvalidUrlException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(NoHashedUrlException.class)
    public ResponseEntity<CustomErrorMessage> handleNoHashedUrlException(
            NoHashedUrlException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}
