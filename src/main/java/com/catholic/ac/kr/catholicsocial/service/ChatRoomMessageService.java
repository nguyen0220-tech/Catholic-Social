package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ChatRoomDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ChatRoomMapper;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;
import com.catholic.ac.kr.catholicsocial.repository.ChatRoomMemberRepository;
import com.catholic.ac.kr.catholicsocial.repository.ChatRoomRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomMessageService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoom> getAllChatRoomsByIds(List<Long> ids) {
        return chatRoomRepository.findAllById(ids);
    }

    public ListResponse<ChatRoomDTO> getChatRooms(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ChatRoomProjection> projections = chatRoomMemberRepository.findByUserId(userId, pageable);

        List<ChatRoomProjection> chatRooms = projections.getContent();

        return new ListResponse<>(ChatRoomMapper.chatRoomDTOs(chatRooms),
                new PageInfo(page, size, projections.hasNext()));
    }

    //    1:1
    @Transactional
    public ChatRoom getChatRoom(Long currentUserId, MessageRequest request) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findExistingRoom(currentUserId, request.getRecipientId());

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        User currentUser = EntityUtils.getOrThrow(userRepository.findById(currentUserId), "User");
        User otherUser = EntityUtils.getOrThrow(userRepository.findById(request.getRecipientId()), "User");

        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setRoomName(otherUser.getUserInfo().getFirstName() + " " + otherUser.getUserInfo().getLastName());
        newChatRoom.setLastMessagePreview(request.getMessage());
        newChatRoom.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(newChatRoom);

        ChatRoomMember m1 = new ChatRoomMember();
        m1.setUser(currentUser);
        m1.setChatRoom(newChatRoom);

        ChatRoomMember m2 = new ChatRoomMember();
        m2.setUser(otherUser);
        m2.setChatRoom(newChatRoom);

        chatRoomMemberRepository.saveAll(List.of(m1, m2));

        return newChatRoom;
    }
}
