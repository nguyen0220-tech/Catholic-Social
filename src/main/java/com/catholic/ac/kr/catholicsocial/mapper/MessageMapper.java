package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.projection.MessageProjection;

import java.util.List;

public class MessageMapper {
    public static MessageDTO toMessageDTO(MessageProjection projection){
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setId(projection.getId());
        messageDTO.setSenderId(projection.getSenderId());
        messageDTO.setText(projection.getText());
        messageDTO.setCreatedAt(projection.getCreatedAt());

        return messageDTO;
    }

    public static List<MessageDTO> toMessageDTOList(List<MessageProjection> projections){
        return projections.stream()
                .map(MessageMapper::toMessageDTO)
                .toList();
    }
}
