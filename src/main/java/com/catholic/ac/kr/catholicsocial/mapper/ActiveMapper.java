package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.ActiveDTO;
import com.catholic.ac.kr.catholicsocial.projection.ActiveProjection;

import java.util.List;

public class ActiveMapper {
    public static ActiveDTO toActiveDTO(ActiveProjection projection) {
        ActiveDTO activeDTO = new ActiveDTO();

        activeDTO.setId(projection.getId());
        activeDTO.setEntityId(projection.getEntityId());
        activeDTO.setType(projection.getType());
        activeDTO.setUserId(projection.getUserId());

        return activeDTO;
    }

    public static List<ActiveDTO> toActiveDTO(List<ActiveProjection> projections) {
        return projections.stream()
                .map(ActiveMapper::toActiveDTO)
                .toList();
    }
}
