package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class IntroVideoDTO {
    private Long id;
    private String url;
    private LocalDateTime exp;

    public IntroVideoDTO(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
