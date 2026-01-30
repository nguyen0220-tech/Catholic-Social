package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.RoomUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SocketService {
    private final SimpMessagingTemplate messagingTemplate;

    // Gửi tin nhắn mới vào phòng
    public void sendNewMessage(MessageDTO message) {
        messagingTemplate.convertAndSend("/queue/message" + message.getChatRoomId(), message);
    }

    // Cập nhật danh sách phòng cho tất cả thành viên
    @Async // Gửi bất đồng bộ để không chặn luồng chính
    public void updateRoomList(List<Long> memberIds, RoomUpdateDTO update) {
        for (Long memberId : memberIds) {
            messagingTemplate.convertAndSend("/queue/rooms-" + memberId, update);
        }
    }
}
