package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MessageRequest {
    private Long recipientId;
    private String message;
    private List<MultipartFile> medias = new ArrayList<>();
}
