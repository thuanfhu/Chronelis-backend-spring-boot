package com.devloopsx.chronelis.validation.annotation;

import com.devloopsx.chronelis.validation.validator.LoginIdentifierValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = LoginIdentifierValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginIdentifierValid {
  String message() default "EMAIL_OR_PHONE_REQUIRED";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
