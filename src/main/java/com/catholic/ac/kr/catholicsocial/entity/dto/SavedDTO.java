package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedDTO {
    private Long id;
    private Long userId;
    private Long momentId;

    private int heartCount; //FK resolver
    private MomentGQLDTO moment; //FK resolver
}
