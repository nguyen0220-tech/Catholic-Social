package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.ChatRoomDTO;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;

import java.util.List;
import java.util.stream.Collectors;

public class ChatRoomMapper {
    public static ChatRoomDTO chatRoomDTO(ChatRoomProjection projection) {
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();

        chatRoomDTO.setChatRoomId(projection.getChatRoomId());

        return chatRoomDTO;
    }

    public static List<ChatRoomDTO> chatRoomDTOs(List<ChatRoomProjection> projections) {
        return projections.stream()
                .map(ChatRoomMapper::chatRoomDTO)
                .toList();
    }
}
