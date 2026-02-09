package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageMediaDTO;
import com.catholic.ac.kr.catholicsocial.projection.MessageMediaProjection;

import java.util.List;

public class MessageMediaMapper {
    public static MessageMediaDTO messageMediaDTO(MessageMediaProjection projection) {
        MessageMediaDTO dto = new MessageMediaDTO();

        dto.setId(projection.getId());
        dto.setUrl(projection.getUrl());
        dto.setSenderId(projection.getSenderId());

        return dto;
    }

    public static List<MessageMediaDTO> messageMediaDTOList(List<MessageMediaProjection> projections) {
        return projections.stream()
                .map(MessageMediaMapper::messageMediaDTO)
                .toList();
    }
}
