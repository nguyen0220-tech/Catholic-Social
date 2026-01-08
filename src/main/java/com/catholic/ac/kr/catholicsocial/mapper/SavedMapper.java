package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.SavedDTO;
import com.catholic.ac.kr.catholicsocial.projection.SavedProjection;

import java.util.ArrayList;
import java.util.List;

public class SavedMapper {
    public static SavedDTO toSavedDTO(SavedProjection projection){
        SavedDTO savedDTO = new SavedDTO();

        savedDTO.setId(projection.getId());
        savedDTO.setUserId(projection.getUserId());
        savedDTO.setMomentId(projection.getMomentId());

        return savedDTO;
    }

    public static List<SavedDTO> toSavedDTO(List<SavedProjection> projections){
        return projections.stream()
                .map(SavedMapper::toSavedDTO)
                .toList();
    }
}
