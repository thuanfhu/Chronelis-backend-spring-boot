package com.devloopsx.chronelis.validation.validator;

import com.devloopsx.chronelis.validation.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

  private String passwordField;
  private String confirmPasswordField;

  @Override
  public void initialize(PasswordMatches constraintAnnotation) {
    this.passwordField = constraintAnnotation.passwordField();
    this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    try {
      // Get object Field from DTO using Reflection
      Field passwordField = value.getClass().getDeclaredField(this.passwordField);
      Field confirmPasswordField = value.getClass().getDeclaredField(this.confirmPasswordField);

      passwordField.setAccessible(true); // allow access private fields (DTO fields is private)
      confirmPasswordField.setAccessible(true);

      String password = (String) passwordField.get(value); // get value from fields
      String confirmPassword = (String) confirmPasswordField.get(value);

      if (!Objects.equals(password, confirmPassword)) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("PASSWORD_AND_CONFIRM_MISMATCH")
            .addPropertyNode(this.confirmPasswordField) // set error message to confirmPassword
            .addConstraintViolation();
        return false;
      }

      return true;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return false;
    }
  }
}
