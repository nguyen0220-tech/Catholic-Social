package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.AddMemberRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UpdateChatRoomRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ChatRoomMapper;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;
import com.catholic.ac.kr.catholicsocial.repository.ChatRoomMemberRepository;
import com.catholic.ac.kr.catholicsocial.repository.ChatRoomRepository;
import com.catholic.ac.kr.catholicsocial.repository.FollowRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomType;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FollowRepository followRepository;

    public List<Long> getMemberIdsByChatRoomId(Long chatRoomId) {
        return chatRoomMemberRepository.findMemberIdsByChatRoomId(chatRoomId, ChatRoomMemberStatus.ACTIVE);
    }

    public List<ChatRoomMember> getAllChatRoomMembers(List<Long> chatRoomIds) {
        return chatRoomMemberRepository.findMembersByChatRoomIds(chatRoomIds);
    }

    public List<ChatRoom> getAllChatRoomsByIds(List<Long> ids) {
        return chatRoomRepository.findAllById(ids);
    }

    public ListResponse<ChatRoomDTO> getChatRooms(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ChatRoomProjection> projections = chatRoomMemberRepository.
                findByUserId(userId, ChatRoomMemberStatus.ACTIVE, pageable);

        List<ChatRoomProjection> chatRooms = projections.getContent();

        return new ListResponse<>(ChatRoomMapper.chatRoomDTOs(chatRooms),
                new PageInfo(page, size, projections.hasNext()));
    }

    //    1:1
    @Transactional
    public ChatRoom getChatRoom(Long currentUserId, MessageRequest request) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.
                findExistingRoom(currentUserId, request.getRecipientId(), ChatRoomType.ONE_TO_ONE);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        User currentUser = EntityUtils.getOrThrow(userRepository.findById(currentUserId), "User");
        User otherUser = EntityUtils.getOrThrow(userRepository.findById(request.getRecipientId()), "User");

        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setLastMessagePreview(request.getMessage());
        newChatRoom.setLastMessageAt(LocalDateTime.now());
        newChatRoom.setType(ChatRoomType.ONE_TO_ONE);
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

    public GraphqlResponse<String> updateChatRoom(Long userId, UpdateChatRoomRequest request) {
        boolean roomExisting = chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, request.getChatRoomId(), ChatRoomMemberStatus.ACTIVE);
        if (!roomExisting) {
            throw new AccessDeniedException("forbidden");
        }

        ChatRoom chatRoom = EntityUtils.getOrThrow(
                chatRoomRepository.findById(request.getChatRoomId()), "ChatRoom");

        chatRoom.setRoomName(request.getChatRoomName());
        chatRoom.setDescription(request.getChatRoomDescription());
        chatRoomRepository.save(chatRoom);

        return GraphqlResponse.success("updated success", null);
    }

    public ListResponse<UserForAddRoomChatDTO> getUserForAddRoomChat(Long userId, Long chatRoomId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserForAddRoomChatDTO> userPage = userRepository.findUserForAddRoomChatDTOByUserId(userId, keyword, pageable);

        List<UserForAddRoomChatDTO> rs = userPage.getContent();

        Set<Long> userIdInRoomChat = new HashSet<>(chatRoomMemberRepository.findMemberIdsByChatRoomId(chatRoomId, ChatRoomMemberStatus.ACTIVE));

        for (UserForAddRoomChatDTO user : rs) {
            user.setInRoom(userIdInRoomChat.contains(user.getUserId()));
        }

        return new ListResponse<>(rs, new PageInfo(page, size, userPage.hasNext()));
    }

    public GraphqlResponse<String> addMemberToChatRoom(Long userId, AddMemberRequest request) {
        boolean roomExisting = chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, request.getChatRoomId(), ChatRoomMemberStatus.ACTIVE);
        if (!roomExisting) {
            throw new AccessDeniedException("forbidden");
        }

        if (userId.equals(request.getMemberId()))
            throw new GraphQLException("Cannot send a direct message: send self");

        boolean blockedRecipient = followRepository.checkBlockTwoWay(userId, request.getMemberId(), FollowState.BLOCKED);

        if (blockedRecipient) {
            throw new IllegalStateException("Cannot send a direct message:blocked recipient");
        }

        if (chatRoomMemberRepository.existsByUser_IdAndChatRoom_IdAndStatus(
                request.getMemberId(),
                request.getChatRoomId()
                , ChatRoomMemberStatus.ACTIVE)) {
            throw new GraphQLException("Already have a member");
        }

        Optional<ChatRoomMember> chatRoomMemberOpt = chatRoomMemberRepository
                .findByUser_IdAndChatRoom_Id(request.getMemberId(), request.getChatRoomId());

        if (chatRoomMemberOpt.isPresent()) {
            ChatRoomMember chatRoomMember = chatRoomMemberOpt.get();
            chatRoomMember.setStatus(ChatRoomMemberStatus.ACTIVE);
            chatRoomMember.setCreatedAt(LocalDateTime.now());
            chatRoomMemberRepository.save(chatRoomMember);

            return GraphqlResponse.success("added success", null);
        }

        ChatRoom chatRoom = EntityUtils.getOrThrow(chatRoomRepository.findById(request.getChatRoomId()), "ChatRoom");
        User member = EntityUtils.getOrThrow(userRepository.findById(request.getMemberId()), "User");

        chatRoom.setType(ChatRoomType.GROUP);

        ChatRoomMember newChatRoomMember = new ChatRoomMember();
        newChatRoomMember.setChatRoom(chatRoom);
        newChatRoomMember.setUser(member);

        chatRoomMemberRepository.save(newChatRoomMember);

        return GraphqlResponse.success("added success", null);
    }

    public ListResponse<MemberOfChatRoomDTO> getMembersOfChatRoom(Long userId, Long chatRoomId, int page, int size) {
        boolean chatRoomMemberExisting = chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId, ChatRoomMemberStatus.ACTIVE);
        if (!chatRoomMemberExisting) {
            throw new AccessDeniedException("forbidden");
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<MemberOfChatRoomDTO> chatRoomMemberPage = chatRoomMemberRepository.
                findMembersByChatRoomId(chatRoomId, ChatRoomMemberStatus.ACTIVE, pageable);

        List<MemberOfChatRoomDTO> chatRoomMembers = chatRoomMemberPage.getContent();

        Set<Long> userIdsBlock = new HashSet<>(followRepository.findUserIdsBlocked(userId));

        List<MemberOfChatRoomDTO> rs = chatRoomMembers.stream()
                .filter(u -> !userIdsBlock.contains(u.getUserId()))
                .toList();

        return new ListResponse<>(rs, new PageInfo(page, size, chatRoomMemberPage.hasNext()));
    }

    public GraphqlResponse<String> leaveChatRoom(Long userId, Long chatRoomId) {
        boolean existing = chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId,ChatRoomMemberStatus.ACTIVE);
        if (!existing) {
            throw new AccessDeniedException("forbidden");
        }

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByUser_IdAndChatRoom_Id(userId, chatRoomId)
                .orElseThrow(() -> new GraphQLException("Cannot find chat room member"));

        if (chatRoomMember.getStatus().equals(ChatRoomMemberStatus.LEAVE))
            throw new GraphQLException("Cannot leave chat room");

        chatRoomMember.setStatus(ChatRoomMemberStatus.LEAVE);
        chatRoomMemberRepository.save(chatRoomMember);

        return GraphqlResponse.success("leave success", null);
    }
}
