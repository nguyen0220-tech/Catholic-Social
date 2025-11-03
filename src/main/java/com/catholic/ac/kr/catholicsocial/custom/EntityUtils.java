package com.catholic.ac.kr.catholicsocial.custom;

import com.catholic.ac.kr.catholicsocial.exception.ResourceNotFoundException;

import java.util.Optional;

public class EntityUtils {

    public static <T> T getOrThrow(Optional<T> opt, String entityName) {
        return opt.orElseThrow(() ->
                new ResourceNotFoundException(entityName + " not found")
        );
    }
}
