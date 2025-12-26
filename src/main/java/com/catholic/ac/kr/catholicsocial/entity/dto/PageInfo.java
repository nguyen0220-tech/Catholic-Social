package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PageInfo {
    private int page;
    private int size;
    private boolean hasNext;
}
