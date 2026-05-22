package com.devloopsx.chronelis.validation.validator;

import com.devloopsx.chronelis.dto.request.auth.LoginRequest;
import com.devloopsx.chronelis.validation.annotation.LoginIdentifierValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LoginIdentifierValidator
    implements ConstraintValidator<LoginIdentifierValid, LoginRequest> {

  @Override
  public boolean isValid(LoginRequest loginRequest, ConstraintValidatorContext context) {
    String email = loginRequest.getEmail();
    String phoneNumber = loginRequest.getPhoneNumber();

    if ((email == null || email.isEmpty()) && (phoneNumber == null || phoneNumber.isEmpty())) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("EMAIL_OR_PHONE_REQUIRED")
          .addConstraintViolation();
      return false;
    }

    if (email != null && !email.isEmpty() && phoneNumber != null && !phoneNumber.isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("ONLY_EMAIL_OR_PHONE").addConstraintViolation();
      return false;
    }

    return true;
  }
}
