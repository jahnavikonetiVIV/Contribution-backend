package com.jahnavi.contribution.exception;

import com.jahnavi.contribution.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse> handleCoreException(CoreException ex) {
        log.warn("CoreException: {} - {}", ex.getStatusCode(), ex.getMessage());
        int statusCode = ex.getStatusCode();
        HttpStatus httpStatus;
        try {
            httpStatus = (statusCode >= 400 && statusCode < 600) ? HttpStatus.valueOf(statusCode) : HttpStatus.BAD_REQUEST;
        } catch (IllegalArgumentException e) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        ApiResponse body = ApiResponse.builder()
                .error(true)
                .message(ex.getMessage())
                .data(null)
                .statusCode(statusCode)
                .build();
        return ResponseEntity.status(httpStatus).body(body);
    }
}
