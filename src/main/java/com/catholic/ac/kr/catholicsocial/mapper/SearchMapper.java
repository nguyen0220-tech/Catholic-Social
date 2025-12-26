package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.SearchDTO;
import com.catholic.ac.kr.catholicsocial.projection.SearchProjection;

import java.util.List;

public class SearchMapper {
    public static SearchDTO searchDTO(SearchProjection projection) {
        SearchDTO searchDTO = new SearchDTO();

        searchDTO.setId(projection.getId());
        searchDTO.setKeyword(projection.getKeyword());
        searchDTO.setCreatedAt(projection.getCreatedAt());

        return searchDTO;
    }

    public static List<SearchDTO> searchDTO(List<SearchProjection> projections) {
        return projections.stream()
                .map(SearchMapper::searchDTO)
                .toList();
    }

}
