package com.devloopsx.chronelis.exception;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private ResponseEntity<ApiResponse<?>> buildErrorResponse(
      ErrorCode errorCode,
      Optional<String> customMessage,
      List<ErrorDetail> errorDetails,
      HttpServletRequest request) {
    if (errorDetails == null || errorDetails.isEmpty()) {
      ErrorDetail detail =
          ErrorDetail.builder()
              .code(errorCode.getCode())
              .message(customMessage.orElse(errorCode.getMessage()))
              .build();
      errorDetails = List.of(detail);
    }
    ApiResponse<?> apiResponse =
        ApiResponse.builder()
            .success(false)
            .errors(errorDetails)
            .meta(buildMetaInfo(request))
            .build();
    return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ApiResponse<?>> handleRuntimeException(
      RuntimeException exception, HttpServletRequest request) {
    log.error("Exception: ", exception);
    return buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, Optional.empty(), null, request);
  }

  @ExceptionHandler(value = ApplicationException.class)
  public ResponseEntity<ApiResponse<?>> handleAppException(
      ApplicationException exception, HttpServletRequest request) {
    String message =
        (exception.getCustomMessage() != null)
            ? exception.getCustomMessage()
            : exception.getErrorCode().getMessage();
    return buildErrorResponse(exception.getErrorCode(), Optional.of(message), null, request);
  }

  @ExceptionHandler(value = AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
      AccessDeniedException exception, HttpServletRequest request) {
    return buildErrorResponse(ErrorCode.UNAUTHORIZED, Optional.empty(), null, request);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    log.error("Validation error: {}", exception.getMessage());
    List<ErrorDetail> errors = ErrorDetailMapper.mapValidationErrors(exception);
    return buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA, Optional.empty(), errors, request);
  }
}
