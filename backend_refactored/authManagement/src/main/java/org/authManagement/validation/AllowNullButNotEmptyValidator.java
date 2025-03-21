package org.authManagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowNullButNotEmptyValidator implements ConstraintValidator<AllowNullButNotEmpty, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null is valid, empty string is invalid
        return value == null || !value.isEmpty();
    }
} 