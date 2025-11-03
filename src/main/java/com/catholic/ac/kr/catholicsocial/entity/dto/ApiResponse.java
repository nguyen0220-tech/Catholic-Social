package com.catholic.ac.kr.catholicsocial.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //tránh field null xuất hiện trong JSON
@Getter
@Setter
public class ApiResponse<T> {
    LocalDateTime timestamp;
    private int status;
    private boolean success;
    private String code;
    private String message;
    private String path;
    private T data;

    //response for RestAPI
    public static <T> ApiResponse<T> success(int status, String code, String message, T data) {
        return new ApiResponse<>(
                LocalDateTime.now(), status, true, code,
                message, null, data);
    }

    public static <T> ApiResponse<T> success(int status, String code, String message) {
        return new ApiResponse<>(
                LocalDateTime.now(), status, true, code,
                message, null, null);
    }

    public static <T> ApiResponse<T> fail(int status, String code, String message) {
        return new ApiResponse<>(LocalDateTime.now(), status, false, code, message, null, null);
    }

    //response for exception
    public static <T> ApiResponse<T> exception(int status, String code, String message, String path) {
        return new ApiResponse<>(
                LocalDateTime.now(), status, false,
                code, message, path, null);
    }

    public static <T> ApiResponse<T> exception(int status, String code, String message, String path, T data) {
        return new ApiResponse<>(
                LocalDateTime.now(), status, false
                , code, message, path, data);
    }
}
