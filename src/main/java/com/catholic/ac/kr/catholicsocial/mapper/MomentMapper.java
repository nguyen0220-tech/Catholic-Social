package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDetailDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Image;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.projection.MomentProjection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MomentMapper {
    public static MomentDTO toPostDTO(Moment moment) {
        MomentDTO momentDTO = new MomentDTO();

        momentDTO.setId(moment.getId());
        momentDTO.setUserId(moment.getUser().getId());
        momentDTO.setUserFullName(moment.getUser().getUserInfo().getFirstName() + " " + moment.getUser().getUserInfo().getLastName());
        momentDTO.setUserAvatar(moment.getUser().getUserInfo().getAvatarUrl());
        momentDTO.setContent(moment.getContent());
        momentDTO.setImageUrls(moment.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList()));
        momentDTO.setShare(String.valueOf(moment.getShare()));
        momentDTO.setEdited(moment.isEdited());
        momentDTO.setCreatedAt(moment.getCreatedAt());

        return momentDTO;
    }

    public static List<MomentDTO> toListDTO(List<Moment> moments) {
        List<MomentDTO> momentDTOS = new ArrayList<>();

        for (Moment moment : moments) {
            momentDTOS.add(toPostDTO(moment));
        }

        return momentDTOS;
    }

    public static MomentDetailDTO toMomentDetailDTO(MomentProjection projection) {
        MomentDetailDTO momentDetailDTO = new MomentDetailDTO();

        momentDetailDTO.setId(projection.getId());
        momentDetailDTO.setActorId(projection.getUserId());

        return momentDetailDTO;
    }
}
