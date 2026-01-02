package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MomentDetailDTO {
    private Long id;
    private boolean isHeart;
    private UserGQLDTO user;
    private MomentUserDTO moment;
}
