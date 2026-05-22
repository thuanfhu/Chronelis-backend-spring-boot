package com.devloopsx.chronelis.exception;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Utility class for mapping validation errors to ErrorDetail objects Supports both REST and
 * WebSocket validation errors
 */
public class ErrorDetailMapper {

  /** Map validation errors from REST Controller (@RestControllerAdvice) */
  public static List<ErrorDetail> mapValidationErrors(MethodArgumentNotValidException exception) {
    return mapValidationErrorsFromBindingResult(exception.getBindingResult());
  }

  /** Map validation errors from WebSocket Controller (@ControllerAdvice for WebSocket) */
  public static List<ErrorDetail> mapWebSocketValidationErrors(
      org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException
          exception) {
    return mapValidationErrorsFromBindingResult(exception.getBindingResult());
  }

  /**
   * Common method to map validation errors from BindingResult Reused by both REST and WebSocket
   * error mappers
   *
   * @param bindingResult The binding result containing validation errors
   * @return List of ErrorDetail objects
   */
  private static List<ErrorDetail> mapValidationErrorsFromBindingResult(
      BindingResult bindingResult) {
    return bindingResult.getAllErrors().stream()
        .map(ErrorDetailMapper::mapSingleError)
        .collect(Collectors.toList());
  }

  /**
   * Map a single ObjectError to ErrorDetail
   *
   * @param error The validation error
   * @return ErrorDetail object
   */
  private static ErrorDetail mapSingleError(ObjectError error) {
    ErrorCode errorCode;
    try {
      errorCode = ErrorCode.valueOf(error.getDefaultMessage());
    } catch (IllegalArgumentException e) {
      errorCode = ErrorCode.INVALID_KEY;
    }

    String resource = null;
    String field = null;
    if (error instanceof FieldError fieldError) {
      resource = fieldError.getObjectName();
      field = fieldError.getField();
    }

    return ErrorDetail.builder()
        .resource(resource)
        .field(field)
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();
  }
}
