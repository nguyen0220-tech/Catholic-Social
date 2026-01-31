package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.ChatRoomDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.RoomChatDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;

import java.util.List;

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

    public static RoomChatDTO roomChatDTO(ChatRoom chatRoom) {
        RoomChatDTO roomChatDTO = new RoomChatDTO();

        roomChatDTO.setChatRoomId(chatRoom.getId());
        roomChatDTO.setRoomName(chatRoom.getRoomName());
        roomChatDTO.setRoomDescription(chatRoom.getDescription());
        roomChatDTO.setLastMessagePreview(chatRoom.getLastMessagePreview());
        roomChatDTO.setLastMessageAt(chatRoom.getLastMessageAt());

        return roomChatDTO;
    }
}
