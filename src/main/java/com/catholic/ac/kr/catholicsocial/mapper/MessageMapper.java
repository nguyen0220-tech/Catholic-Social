package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Message;
import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.projection.MessageProjection;

import java.util.List;

public class MessageMapper {
    public static MessageDTO toMessageDTO(MessageProjection projection) {
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setId(projection.getId());
        messageDTO.setSenderId(projection.getSenderId());
        messageDTO.setText(projection.getText());
        messageDTO.setCreatedAt(projection.getCreatedAt());

        return messageDTO;
    }

    public static List<MessageDTO> toMessageDTOList(List<MessageProjection> projections) {
        return projections.stream()
                .map(MessageMapper::toMessageDTO)
                .toList();
    }

    public static MessageDTO toMessageDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setId(message.getId());
        messageDTO.setSenderId(message.getSender().getId());
        messageDTO.setText(message.getText());
        messageDTO.setCreatedAt(message.getCreatedAt());

        if (!(message.getMedias().isEmpty())) {
            List<String> medias = message.getMedias()
                    .stream()
                    .map(MessageMedia::getUrl)
                    .toList();
            messageDTO.setMedia(medias);
        }

        return messageDTO;
    }
}
