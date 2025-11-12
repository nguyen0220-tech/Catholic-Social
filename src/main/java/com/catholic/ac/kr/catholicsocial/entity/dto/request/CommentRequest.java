package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentRequest {
    @NotBlank(message = "Nội dung không được để trống")
    private String comment;
}
