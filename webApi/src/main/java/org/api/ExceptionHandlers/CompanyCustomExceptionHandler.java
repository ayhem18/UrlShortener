package org.api.ExceptionHandlers;

import org.api.Requests.ExistingCompanyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.utils.CustomErrorMessage;

@ControllerAdvice
public class CompanyCustomExceptionHandler extends CustomExceptionHandler {

    @ExceptionHandler(ExistingCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleExistingCompanyException(
            ExistingCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoCompanyException.class)
    public ResponseEntity<CustomErrorMessage> handleNoCompanyException(
            NoCompanyException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}
