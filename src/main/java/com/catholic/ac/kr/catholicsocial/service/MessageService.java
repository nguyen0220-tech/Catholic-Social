package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
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
    private final ChatRoomMessageService chatRoomMessageService;
    private final UserRepository userRepository;
    private final UploadFileHandler uploadFileHandler;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FollowRepository followRepository;
    private final FollowService followService;

    public ListResponse<MessageDTO> getMessages(Long userId, Long chatRoomId, int page, int size) {
        if (!chatRoomMemberRepository
                .existsByUser_IdAndChatRoom_IdAndStatus(userId, chatRoomId, ChatRoomMemberStatus.ACTIVE)) {
            throw new AccessDeniedException("forbidden");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Page<MessageProjection> projections = messageRepository.findByChatRoomId(chatRoomId, pageable);

        List<Long> userIdsBlock = followService.getUserIdsBlocked(userId);

        Set<Long> setIdBlock = new HashSet<>(userIdsBlock);

        List<MessageProjection> projectionList = projections.getContent();

        List<MessageProjection> rs = projectionList.stream()
                .filter(m -> !setIdBlock.contains(m.getSenderId()))
                .toList();

        List<MessageDTO> messageDTOS = MessageMapper.toMessageDTOList(rs);

        return new ListResponse<>(messageDTOS, new PageInfo(page, size, projections.hasNext()));
    }

    @Transactional
    public ApiResponse<String> sendDirectMessage(Long userId, MessageRequest request) {
        if (userId.equals(request.getRecipientId()))
            throw new IllegalStateException("Cannot send a direct message: send self");

        boolean blockedRecipient = followRepository.checkBlockTwoWay(userId, request.getRecipientId(), FollowState.BLOCKED);

        if (blockedRecipient) {
            throw new IllegalStateException("Cannot send a direct message:blocked recipient");
        }

        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        ChatRoom room = chatRoomMessageService.getChatRoom(userId, request);

        Message message = new Message();
        message.setSender(user);
        message.setChatRoom(room);
        message.setText(request.getMessage());

        if (!(request.getMedias().isEmpty())) {
            List<MessageMedia> list = new ArrayList<>();

            for (MultipartFile media : request.getMedias()) {
                MessageMedia messageMedia = new MessageMedia();
                messageMedia.setMessage(message);
                messageMedia.setUser(user);
                messageMedia.setUrl(uploadFileHandler.uploadFile(userId, media));

                list.add(messageMedia);
            }
            message.setListImage(list);
        }

        messageRepository.save(message);

        room.setLastMessagePreview(request.getMessage() != null ? request.getMessage() : "[Link]");
        room.setLastMessageAt(message.getCreatedAt());
        chatRoomRepository.save(room);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Message sent successfully");

    }
}
