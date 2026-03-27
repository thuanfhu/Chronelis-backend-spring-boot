package com.devloopsx.chronelis.exception;

import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Global WebSocket Exception Handler
 * 
 * Handles exceptions from NON-ASYNC WebSocket message handlers
 * For @Async methods, exceptions must be caught manually in the controller
 * because @MessageExceptionHandler cannot intercept exceptions from async
 * threads
 */
@ControllerAdvice
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    private ApiResponse<?> buildErrorResponse(ErrorCode errorCode, Optional<String> customMessage,
            List<ErrorDetail> errorDetails) {
        List<ErrorDetail> details = (errorDetails == null || errorDetails.isEmpty())
                ? List.of(ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(customMessage.orElse(errorCode.getMessage()))
                        .build())
                : errorDetails;

        return ApiResponse.builder()
                .success(false)
                .errors(details)
                .meta(null)
                .build();
    }

    /**
     * Handle ApplicationException (business logic errors)
     */
    @MessageExceptionHandler(ApplicationException.class)
    @SendToUser("/private/errors")
    public ApiResponse<?> handleApplicationException(ApplicationException ex,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = getUserId(headerAccessor);
        log.error("WebSocket ApplicationException for userId={}: {}", userId, ex.getMessage());

        String message = ex.getCustomMessage() != null ? ex.getCustomMessage() : ex.getErrorCode().getMessage();
        return buildErrorResponse(ex.getErrorCode(), Optional.of(message), null);
    }

    /**
     * Handle MethodArgumentNotValidException (validation errors from @Valid)
     * This catches validation errors BEFORE the method is invoked
     */
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/private/errors")
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException ex,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = getUserId(headerAccessor);
        log.error("WebSocket ValidationException for userId={}: {}", userId, ex.getMessage());

        List<ErrorDetail> errorDetails = ErrorDetailMapper.mapWebSocketValidationErrors(ex);
        return buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA, Optional.empty(), errorDetails);
    }

    /**
     * Handle MessageConversionException (invalid JSON format)
     */
    @MessageExceptionHandler(MessageConversionException.class)
    @SendToUser("/private/errors")
    public ApiResponse<?> handleMessageConversionException(MessageConversionException ex,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = getUserId(headerAccessor);
        log.error("WebSocket MessageConversionException for userId={}: {}", userId, ex.getMessage());

        return buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA,
                Optional.of("Định dạng dữ liệu không hợp lệ"), null);
    }

    /**
     * Handle all other exceptions
     */
    @MessageExceptionHandler(Throwable.class)
    @SendToUser("/private/errors")
    public ApiResponse<?> handleAllExceptions(Throwable ex, SimpMessageHeaderAccessor headerAccessor) {
        String userId = getUserId(headerAccessor);
        log.error("WebSocket Unhandled error for userId={}: {}", userId, ex.getMessage(), ex);

        return buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION,
                Optional.of("Đã xảy ra lỗi không mong muốn"), null);
    }

    /**
     * Get authenticated userId from Principal (set by UserInterceptor after JWT
     * validation)
     */
    private String getUserId(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        String userId = (principal != null) ? principal.getName() : "unknown";
        log.debug("Retrieved userId from Principal: {}", userId);
        return userId;
    }

    /**
     * Public method to send error to user (used by async methods)
     */
    public void sendErrorToUser(String userId, ErrorCode errorCode, String message) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();

        ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .errors(List.of(errorDetail))
                .meta(null)
                .build();

        String destination = "/private/errors";
        messagingTemplate.convertAndSendToUser(userId, destination, errorResponse);
        log.info("Sent error to user {} at /user/{}{} - Code: {} - Message: {}",
                userId, userId, destination, errorCode.getCode(), message);
    }

    /**
     * Public method to send validation errors to user (used by async methods)
     */
    public void sendValidationErrorsToUser(String userId, List<ErrorDetail> errorDetails) {
        ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .errors(errorDetails)
                .meta(null)
                .build();

        String destination = "/private/errors";
        messagingTemplate.convertAndSendToUser(userId, destination, errorResponse);
        log.info("Sent {} validation errors to user {} at /user/{}{}",
                errorDetails.size(), userId, userId, destination);
    }
}
