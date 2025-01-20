package org.api.ExceptionHandlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;

import java.time.LocalDateTime;

public abstract class CustomExceptionHandler {
    protected ResponseEntity<CustomErrorMessage> handle(
            RuntimeException e, WebRequest request, HttpStatus s) {
        CustomErrorMessage body = new CustomErrorMessage(
                s.value(),
                LocalDateTime.now(),
                e.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(body, s);
    }
}
