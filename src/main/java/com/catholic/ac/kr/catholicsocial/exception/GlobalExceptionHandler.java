package com.catholic.ac.kr.catholicsocial.exception;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //check input
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>> handleValidationException(MethodArgumentNotValidException  exception) {
        Map<String,String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String,String>> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setMessage("Validation Failed");
        apiResponse.setData(errors);

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiException> handleNoSuchElementException(NoSuchElementException e) {
        ApiException exception = new ApiException(
                HttpStatus.NOT_FOUND.value(),
                "data not foud" + e.getMessage()
                );

        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoudException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(ResourceNotFoudException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("data not found" + e.getMessage()));
    }

    // 401 - Chưa đăng nhập hoặc token sai
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ApiException>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized: "+ex.getMessage()));
    }

    // 403 - Không đủ quyền truy cập
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ApiException>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Forbidden: "+ex.getMessage()));
    }
    // 423 - Tài khoản bị khóa
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<ApiException>> handleLockedException(LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ApiResponse.error("Locked: "+ex.getMessage()));
    }

    // 409 - Dữ liệu trùng (ví dụ role đã tồn tại)
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ApiException>> handleAlreadyExistsException(AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Already exists: "+ex.getMessage()));
    }

    // General
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handlerGeneralException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "Lỗi hệ thống: " + e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
