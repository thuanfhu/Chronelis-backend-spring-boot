package com.devloopsx.chronelis.validation.annotation;

import com.devloopsx.chronelis.validation.validator.PasswordMatchesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
  String message() default "PASSWORD_AND_CONFIRM_MISMATCH";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String passwordField();

  String confirmPasswordField();
}
