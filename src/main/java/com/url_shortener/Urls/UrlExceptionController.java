package com.url_shortener.Urls;

import com.url_shortener.CustomErrorMessage;
import com.url_shortener.CustomExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

// create a hierarchy of exceptions for the Subscription related endpoints
// different exception same handle function

abstract class SubscriptionViolatedException extends RuntimeException {
    public SubscriptionViolatedException(String message) {
        super(message);
    }
}

class MaxNumLevelsSubExceeded extends SubscriptionViolatedException {

    public MaxNumLevelsSubExceeded(String message) {
        super(message);
    }

    public MaxNumLevelsSubExceeded(int numLevels, int maxNumLevels) {
        super("The number of levels " + numLevels + " exceeds the subscription limit " + maxNumLevels);
    }
}

class LevelNamesSubExceeded extends SubscriptionViolatedException {

    public LevelNamesSubExceeded(String message) {
        super(message);
    }

    public LevelNamesSubExceeded(int numLevelNames, int maxNumLevelNames, int exceedingLevel) {
        super("The number of unique level names at level "
                + exceedingLevel + " is: "
                + numLevelNames + " which exceeds the subscription limit " + maxNumLevelNames);
    }
}

class LevelPathVariablesSubExceeded extends SubscriptionViolatedException {

    public LevelPathVariablesSubExceeded(String message) {
        super(message);
    }

    public LevelPathVariablesSubExceeded(int num, int max, int exceedingLevel) {
        super("The number of unique path variables at level "
                + exceedingLevel + " is: "
                + num + " which exceeds the subscription limit " + max);
    }
}

class QueryParametersSubExceeded extends SubscriptionViolatedException {
    public QueryParametersSubExceeded(String message) {
        super(message);
    }

    public QueryParametersSubExceeded(int numLevelNames, int maxNumLevelNames, int exceedingLevel) {
        super("The number of unique query parameters at level "
                + exceedingLevel + " is: "
                + numLevelNames + " which exceeds the subscription limit " + maxNumLevelNames);
    }
}

class QueryParametersValuesSubExceeded extends SubscriptionViolatedException {

    public QueryParametersValuesSubExceeded(String message) {
        super(message);
    }

    public QueryParametersValuesSubExceeded(int numLevelNames, int maxNumLevelNames, int exceedingLevel) {
        super("The number of query parameter unique values at level "
                + exceedingLevel + " is: "
                + numLevelNames + " which exceeds the subscription limit " + maxNumLevelNames);
    }
}




@ControllerAdvice
public class UrlExceptionController extends CustomExceptionHandler {
    @ExceptionHandler(SubscriptionViolatedException.class)
    public ResponseEntity<CustomErrorMessage> handleSubscriptionViolatedException(
            SubscriptionViolatedException e, WebRequest request) {
        return handle(e, request, HttpStatus.BAD_REQUEST);
    }

}
