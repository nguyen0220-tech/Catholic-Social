package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Heart;

import java.util.ArrayList;
import java.util.List;

public class HeartMapper {
    public static HeartDTO toHeartDTO(Heart heart) {
        HeartDTO heartDTO = new HeartDTO();

        heartDTO.setHeartId(heart.getId());
        UserGQLDTO userGQLDTO = ConvertHandler.convertToUserGQLDTO(heart.getUser());
        heartDTO.setUser(userGQLDTO);
        return heartDTO;
    }

    public static List<HeartDTO> toListHeartDTO(List<Heart> hearts) {
        List<HeartDTO> heartDTOs = new ArrayList<>();

        for (Heart heart : hearts) {
            heartDTOs.add(toHeartDTO(heart));
        }
        return heartDTOs;
    }
}
