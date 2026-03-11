package com.vivriti.investron.common.utils;

import com.vivriti.investron.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class CoreUtil {

    private CoreUtil() {
    }

    public static ResponseEntity<ApiResponse> buildApiResponse(
            Object data,
            HttpServletRequest request,
            String message,
            int successCode,
            int failureCode,
            HttpStatus successStatus,
            HttpStatus failureStatus) {
        return ResponseEntity.status(successStatus)
                .body(ApiResponse.builder()
                        .error(false)
                        .message(message)
                        .data(data)
                        .statusCode(successCode)
                        .build());
    }

    public static ResponseEntity<ApiResponse> buildApiResponse(
            Object data,
            boolean error,
            String message,
            HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.builder()
                        .error(error)
                        .message(message)
                        .data(data)
                        .statusCode(status.value())
                        .build());
    }
}
