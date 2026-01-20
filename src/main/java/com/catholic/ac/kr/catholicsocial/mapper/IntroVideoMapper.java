package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.IntroVideo;
import com.catholic.ac.kr.catholicsocial.projection.IntroProjection;

import java.util.List;

public class IntroVideoMapper {
    public static IntroVideoDTO toIntroVideoDTO(IntroVideo introVideo) {
        IntroVideoDTO introVideoDTO = new IntroVideoDTO();

        introVideoDTO.setId(introVideo.getId());
        introVideoDTO.setUrl(introVideo.getUrl());
        introVideoDTO.setExp(introVideo.getExp());

        return introVideoDTO;
    }

    public static IntroVideoDTO introVideoDTO(IntroProjection projection) {
        IntroVideoDTO introVideoDTO = new IntroVideoDTO();

        introVideoDTO.setId(projection.getId());

        return introVideoDTO;
    }

    public static List<IntroVideoDTO > toIntroVideoDTOList(List<IntroProjection> projectionList) {
        return projectionList.stream()
                .map(IntroVideoMapper::introVideoDTO)
                .toList();
    }
}
