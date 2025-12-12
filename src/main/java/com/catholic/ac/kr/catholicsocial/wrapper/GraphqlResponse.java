package com.catholic.ac.kr.catholicsocial.wrapper;

import lombok.Getter;
import lombok.Setter;

//use Mutation
@Getter
@Setter
public class GraphqlResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public GraphqlResponse(boolean success, String message, T data) {
        this.data = data;
        this.success = success;
        this.message = message;
    }

    public static <T> GraphqlResponse<T> success(String message, T data) {
        return new GraphqlResponse<>(true, message, data);
    }
}
