package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;

import com.catholic.ac.kr.catholicsocial.projection.HeartProjection;

import java.util.List;

public class HeartMapper {
    public static HeartDTO toHeartDTO(HeartProjection projection) {
        HeartDTO heartDTO = new HeartDTO();

        heartDTO.setHeartId(projection.getId());
        heartDTO.setUserId(projection.getUserId());

        return heartDTO;
    }

    public static List<HeartDTO> toListHeartDTO(List<HeartProjection> projections) {
        return projections.stream()
                .map(HeartMapper::toHeartDTO)
                .toList();
    }
}
