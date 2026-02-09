package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.AddMemberRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.GroupChatRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UpdateChatRoomRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.entity.model.LogChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ChatRoomMapper;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;
import com.catholic.ac.kr.catholicsocial.repository.*;
import com.catholic.ac.kr.catholicsocial.service.hepler.EntityUtils;
import com.catholic.ac.kr.catholicsocial.service.hepler.HelperService;
import com.catholic.ac.kr.catholicsocial.service.hepler.SocketService;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomType;
import com.catholic.ac.kr.catholicsocial.status.LogRoomContent;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FollowRepository followRepository;
    private final FollowService followService;
    private final SocketService socketService;
    private final MessageRepository messageRepository;
    private final LogChatRoomRepository logChatRoomRepository;
    private final HelperService helperService;

    public Map<Long, ChatRoom> getAllChatRoomWithUsers(List<Long> recipientIds, Long currentUserId) {
        List<Object[]> chatRooms = chatRoomMemberRepository
                .findAllByRecipientIdsAndUserId(recipientIds, currentUserId, ChatRoomType.ONE_TO_ONE);

        Map<Long, ChatRoom> result = new HashMap<>();

        for (Object[] row : chatRooms) {
            result.put((Long) row[0], (ChatRoom) row[1]);
        }
        return result;
    }

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

    // current user send message to 10 other recent
    public List<UserRecentMessageDTO> getUsersRecentMessage(Long userId) {
        Pageable pageable = PageRequest.of(0, 10);

        return messageRepository.findRecentUsers(userId, pageable).stream()
                .map(UserRecentMessageDTO::new)
                .toList();
    }

    public ListResponse<UserForCreateRoomChatDTO> getUsersForCreateRoom(Long userId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserForCreateRoomChatDTO> usersPage = userRepository.
                findUserForCreateRoomChatDTOByUserId(userId, keyword, pageable);

        List<UserForCreateRoomChatDTO> users = usersPage.getContent();

        return new ListResponse<>(users, new PageInfo(page, size, usersPage.hasNext()));
    }

    @Transactional
    public GraphqlResponse<RoomChatDTO> createGroupChat(Long userId, GroupChatRequest request) {
        if (request.getMemberIds().isEmpty()) {
            throw new GraphQLException("Invalid request: memberIds is empty");
        }

        List<Long> userIdsBlock = followService.getUserIdsBlocked(userId);
        for (Long memberId : request.getMemberIds()) {
            if (userIdsBlock.contains(memberId)) {
                throw new GraphQLException("Cannot create group chat because user already block");
            }
        }

        User currentUser = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setRoomName(request.getRoomName());
        newChatRoom.setDescription(request.getRoomDescription());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        newChatRoom.setLastMessagePreview("[New Chat Room Created At: " + LocalDateTime.now().format(formatter) + "]");
        newChatRoom.setLastMessageAt(LocalDateTime.now());
        newChatRoom.setType(ChatRoomType.GROUP);
        chatRoomRepository.save(newChatRoom);

        List<ChatRoomMember> chatRoomMembers = new ArrayList<>();

        ChatRoomMember myChatRoomMember = new ChatRoomMember();
        myChatRoomMember.setChatRoom(newChatRoom);
        myChatRoomMember.setUser(currentUser);

        chatRoomMembers.add(myChatRoomMember);

        for (Long memberId : request.getMemberIds()) {
            User member = EntityUtils.getOrThrow(userRepository.findById(memberId), "User");

            ChatRoomMember chatRoomMember = new ChatRoomMember();

            chatRoomMember.setChatRoom(newChatRoom);
            chatRoomMember.setUser(member);

            chatRoomMembers.add(chatRoomMember);
        }

        chatRoomMemberRepository.saveAll(chatRoomMembers);

        LogChatRoom log = new LogChatRoom();
        log.setChatRoom(newChatRoom);
        log.setActorId(userId);
        log.setContent(LogRoomContent.CREATED_ROOM_CHAT);
        logChatRoomRepository.save(log);

        RoomChatDTO roomChatDTO = ChatRoomMapper.roomChatDTO(newChatRoom);

        socketService.creatOrUpdateChat(request.getMemberIds(), roomChatDTO); //socket to members
        socketService.creatOrUpdateChat(userId, roomChatDTO); //soket to current user
        return GraphqlResponse.success("Created chat room success", roomChatDTO);
    }

    //    1:1
    @Transactional
    public ChatRoom getChatRoom(Long currentUserId, MessageRequest request) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.
                findExistingRoom(currentUserId, request.getRecipientId(), ChatRoomType.ONE_TO_ONE);

        if (existingRoom.isPresent()) {
            List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(existingRoom.get());
            chatRoomMembers.forEach(
                    m -> {
                        if (m.getStatus().equals(ChatRoomMemberStatus.LEAVE)) {
                            m.setStatus(ChatRoomMemberStatus.ACTIVE);
                        }
                    });

            chatRoomMemberRepository.saveAll(chatRoomMembers);

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

    public GraphqlResponse<RoomChatDTO> updateChatRoom(Long userId, UpdateChatRoomRequest request) {
        helperService.validateMember(userId, request.getChatRoomId());

        ChatRoom chatRoom = EntityUtils.getOrThrow(
                chatRoomRepository.findById(request.getChatRoomId()), "ChatRoom");

        chatRoom.setRoomName(request.getChatRoomName());
        chatRoom.setDescription(request.getChatRoomDescription());
        chatRoomRepository.save(chatRoom);

        LogChatRoom log = new LogChatRoom();
        log.setChatRoom(chatRoom);
        log.setContent(LogRoomContent.UPDATED_ROOM_CHAT);
        log.setActorId(userId);
        logChatRoomRepository.save(log);

        //socket
        RoomChatDTO roomChatDTO = ChatRoomMapper.roomChatDTO(chatRoom);

        List<Long> memberIds = chatRoomMemberRepository.findMemberIdsByChatRoomId(chatRoom.getId(), ChatRoomMemberStatus.ACTIVE);

        socketService.creatOrUpdateChat(memberIds, roomChatDTO);

        return GraphqlResponse.success("updated success", roomChatDTO);
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
        helperService.validateMember(userId, request.getChatRoomId());

        helperService.validateDirectMessage(userId, request.getMemberId());

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

        LogChatRoom log = new LogChatRoom();
        log.setChatRoom(chatRoom);
        log.setContent(LogRoomContent.ADDED_MEMBER_ROOM_CHAT);
        log.setActorId(userId);
        logChatRoomRepository.save(log);

        return GraphqlResponse.success("added success", null);
    }

    public ListResponse<MemberOfChatRoomDTO> getMembersOfChatRoom(Long userId, Long chatRoomId, int page, int size) {
        boolean chatRoomMemberExisting = chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId, ChatRoomMemberStatus.ACTIVE);
        if (!chatRoomMemberExisting) {
            throw new AccessDeniedException("forbidden");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

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
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId, ChatRoomMemberStatus.ACTIVE);
        if (!existing) {
            throw new AccessDeniedException("forbidden");
        }

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByUser_IdAndChatRoom_Id(userId, chatRoomId)
                .orElseThrow(() -> new GraphQLException("Cannot find chat room member"));

        if (chatRoomMember.getStatus().equals(ChatRoomMemberStatus.LEAVE))
            throw new GraphQLException("Cannot leave chat room");

        chatRoomMember.setStatus(ChatRoomMemberStatus.LEAVE);
        chatRoomMemberRepository.save(chatRoomMember);

        LogChatRoom log = new LogChatRoom();
        log.setChatRoom(chatRoomMember.getChatRoom());
        log.setContent(LogRoomContent.LEAVE_ROOM_CHAT);
        log.setActorId(userId);
        logChatRoomRepository.save(log);

        return GraphqlResponse.success("leave success", null);
    }
}
