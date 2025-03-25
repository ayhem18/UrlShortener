package org.apiUtils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowNullButNotEmptyValidator.class)
@Documented
public @interface AllowNullButNotEmpty {
    String message() default "can be null but not empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 