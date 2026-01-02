package com.catholic.ac.kr.catholicsocial.wrapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.MomentUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
wrapper class
 */
@Getter @Setter
@AllArgsConstructor
public class MomentConnection {
    private List<MomentUserDTO> content;
    private int page;
    private int size;
}
