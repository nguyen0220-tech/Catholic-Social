package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String text;
    private LocalDateTime createdAt;
    private List<String> media = new ArrayList<>();

}
