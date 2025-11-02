package com.catholic.ac.kr.catholicsocial.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiException {
    private int status;
    private String message;
    private Map<String, String> details;
    LocalDateTime timestamp;

    public ApiException(int status, String message){
        this.status = status;
        this.message = message;
    }
}
