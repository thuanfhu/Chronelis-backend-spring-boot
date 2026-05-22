package com.devloopsx.chronelis.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApplicationException extends RuntimeException {
  ErrorCode errorCode;
  String customMessage;

  public ApplicationException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.customMessage = null;
  }

  public ApplicationException(ErrorCode errorCode, String customMessage) {
    super(customMessage != null ? customMessage : errorCode.getMessage());
    this.errorCode = errorCode;
    this.customMessage = customMessage;
  }
}
