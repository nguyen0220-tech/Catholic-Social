package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class IntroVideoRequest {
    private MultipartFile intro;
    private int expDay;
}
