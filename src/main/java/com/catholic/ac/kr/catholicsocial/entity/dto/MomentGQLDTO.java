package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
    class chứa thông tin moment lấy các comment, heart
 */
@Getter @Setter
public class MomentGQLDTO {
    private Long id;
    private String content;
    private List<String> images = new ArrayList<>();
}
