package com.catholic.ac.kr.catholicsocial.wrapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
//use Query: support DataLoader
@AllArgsConstructor
@Getter @Setter
public class ListResponse<T> {
    private List<T> data;
}
