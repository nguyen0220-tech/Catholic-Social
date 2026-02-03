package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.dto.RoomChatDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.RoomUpdateDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageForRoomChatRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.Message;
import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.MessageMapper;
import com.catholic.ac.kr.catholicsocial.projection.MessageProjection;
import com.catholic.ac.kr.catholicsocial.repository.*;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import com.catholic.ac.kr.catholicsocial.uploadfile.UploadFileHandler;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final UploadFileHandler uploadFileHandler;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FollowRepository followRepository;
    private final FollowService followService;
    private final SocketService socketService;

    public ListResponse<MessageDTO> getMessages(Long userId, Long chatRoomId, int page, int size) {
        if (!chatRoomMemberRepository.existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId, ChatRoomMemberStatus.ACTIVE)) {
            throw new AccessDeniedException("forbidden");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<MessageProjection> projections = messageRepository.findByChatRoomId(chatRoomId, pageable);

        List<Long> userIdsBlock = followService.getUserIdsBlocked(userId);

        Set<Long> setIdBlock = new HashSet<>(userIdsBlock);

        List<MessageProjection> projectionList = projections.getContent();

        List<MessageProjection> rs = projectionList.stream().filter(m -> !setIdBlock.contains(m.getSenderId())).toList();

        List<MessageDTO> messageDTOS = MessageMapper.toMessageDTOList(rs);

        return new ListResponse<>(messageDTOS, new PageInfo(page, size, projections.hasNext()));
    }

    @Transactional
    public ApiResponse<MessageDTO> sendMessageInRoom(Long userId, MessageForRoomChatRequest request) {
        boolean existing = chatRoomMemberRepository.existsByUser_IdAndChatRoom_IdAndStatus(userId, request.getChatRoomId(), ChatRoomMemberStatus.ACTIVE);

        if (!existing) {
            throw new AccessDeniedException("forbidden");
        }
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        ChatRoom room = EntityUtils.getOrThrow(chatRoomRepository.findById(request.getChatRoomId()), "ChatRoom");

        Message newMessage = createMessage(user, room, request.getMessage(), request.getMedias());

        MessageDTO messageDTO = MessageMapper.toMessageDTO(newMessage);

        //Socket
        List<Long> memberIds = chatRoomService.getMemberIdsByChatRoomId(messageDTO.getChatRoomId());

        Set<Long> userIdsBlock = new HashSet<>(followService.getUserIdsBlocked(userId));

        List<Long> filterMemberIds = memberIds.stream()
                .filter(id -> !userIdsBlock.contains(id))
                .toList();

        RoomUpdateDTO roomUpdateDTO = new RoomUpdateDTO(
                messageDTO.getChatRoomId(),
                (messageDTO.getText().isBlank()) ? "[Ảnh]" : messageDTO.getText(),
                messageDTO.getCreatedAt());

        socketService.sendNewMessage(messageDTO); //to member in room
        socketService.updateRoomList(filterMemberIds, roomUpdateDTO); //to list room

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Message sent successfully", messageDTO);

    }

    @Transactional
    public ApiResponse<MessageDTO> sendDirectMessage(Long userId, MessageRequest request) {
        if (userId.equals(request.getRecipientId()))
            throw new IllegalStateException("Cannot send a direct message: send self");

        boolean blockedRecipient = followRepository.checkBlockTwoWay(userId, request.getRecipientId(), FollowState.BLOCKED);

        if (blockedRecipient) {
            throw new IllegalStateException("Cannot send a direct message:blocked recipient");
        }

        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        ChatRoom room = chatRoomService.getChatRoom(userId, request);

        Message newMessage = createMessage(user, room, request.getMessage(), request.getMedias());

        //socket
        MessageDTO messageDTO = MessageMapper.toMessageDTO(newMessage);

        RoomChatDTO roomUpdateDTO = new RoomChatDTO(
                messageDTO.getChatRoomId(),
                user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName(),
                room.getDescription(),
                room.getLastMessagePreview(),
                messageDTO.getCreatedAt()
        );

        socketService.creatOrUpdateChat(request.getRecipientId(), roomUpdateDTO);


        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Message sent successfully", messageDTO);

    }

    private Message createMessage(User sender, ChatRoom chatRoom, String text, List<MultipartFile> medias) {
        Message message = new Message();
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setText(text);

        if (!(medias.isEmpty())) {
            List<MessageMedia> list = new ArrayList<>();

            for (MultipartFile media : medias) {
                MessageMedia messageMedia = new MessageMedia();
                messageMedia.setMessage(message);
                messageMedia.setUser(sender);
                messageMedia.setUrl(uploadFileHandler.uploadFile(sender.getId(), media));

                list.add(messageMedia);
            }
            message.setListImage(list);
        }

        messageRepository.save(message);

        if (text.isBlank()) {
            chatRoom.setLastMessagePreview("[Ảnh]");
        } else
            chatRoom.setLastMessagePreview(text);

        chatRoom.setLastMessageAt(message.getCreatedAt());
        chatRoomRepository.save(chatRoom);

        return message;
    }
}
