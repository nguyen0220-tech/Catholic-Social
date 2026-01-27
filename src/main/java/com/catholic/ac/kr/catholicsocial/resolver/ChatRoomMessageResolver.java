package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UpdateChatRoomRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.BatchLoaderHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.ChatRoomMessageService;
import com.catholic.ac.kr.catholicsocial.service.MessageMediaService;
import com.catholic.ac.kr.catholicsocial.service.MessageService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatRoomMessageResolver {
    private final ChatRoomMessageService chatRoomMessageService;
    private final MessageService messageService;
    private final BatchLoaderHandler batchLoaderHandler;
    private final MessageMediaService messageMediaService;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;

    @QueryMapping
    public ListResponse<ChatRoomDTO> roomsChat(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument int page,
            @Argument int size) {
        return chatRoomMessageService.getChatRooms(useDetails.getUser().getId(), page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> updateChatRoom(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument UpdateChatRoomRequest request
    ) {
        return chatRoomMessageService.updateChatRoom(useDetails.getUser().getId(), request);
    }

    @BatchMapping(typeName = "ChatRoom", field = "detail")
    public Map<ChatRoomDTO, ChatRoomDetailDTO> detail(List<ChatRoomDTO> rooms) {
        List<Long> roomIds = rooms.stream()
                .map(ChatRoomDTO::getChatRoomId)
                .distinct()
                .toList();

        List<ChatRoom> chatRooms = chatRoomMessageService.getAllChatRoomsByIds(roomIds);

        Map<Long, ChatRoomDetailDTO> map = chatRooms.stream()
                .collect(Collectors.toMap(
                        ChatRoom::getId,
                        ConvertHandler::convertToChatRoomDetailDTO
                ));

        return rooms.stream()
                .collect(Collectors.toMap(
                        r -> r,
                        r -> map.get(r.getChatRoomId())
                ));
    }

    @BatchMapping(typeName = "ChatRoom", field = "members")
    public Map<ChatRoomDTO, List<UserGQLDTO>> members(List<ChatRoomDTO> rooms, Principal principal) {
        CustomUserDetails useDetails = userDetailsForBatchMapping.getCustomUserDetails(principal);

        Long currentUserId = useDetails.getUser().getId();

        List<Long> roomIds = rooms.stream()
                .map(ChatRoomDTO::getChatRoomId)
                .distinct()
                .toList();

        List<ChatRoomMember> allMembers = chatRoomMessageService.getAllChatRoomMembers(roomIds);

        List<ChatRoomMember> membersFilter = allMembers.stream()
                .filter(m -> !(m.getUser().getId().equals(currentUserId)))
                .toList();

        Map<Long, List<UserGQLDTO>> map = membersFilter.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getChatRoom().getId(),
                        Collectors.mapping(
                                m -> ConvertHandler.convertToUserGQLDTO(m.getUser()), Collectors.toList())
                ));

        return rooms.stream()
                .collect(Collectors.toMap(
                        r -> r,
                        r -> map.getOrDefault(r.getChatRoomId(), List.of())
                ));
    }

    @QueryMapping
    public ListResponse<UserForAddRoomChatDTO> userForAddRoomChat(
            @Argument Long userId,
            @Argument String keyword,
            @Argument int page,
            @Argument int size
    ) {
        return chatRoomMessageService.getUserForAddRoomChat(userId, keyword, page, size);
    }


    @BatchMapping(typeName = "UserForAddChatRoom", field = "userForChatRoom")
    public Map<UserForAddRoomChatDTO, UserGQLDTO> userForChatRoom(List<UserForAddRoomChatDTO> users) {

        return batchLoaderHandler.batchLoadUser(users, UserForAddRoomChatDTO::getUserId);
    }

    @BatchMapping(typeName = "UserForAddChatRoom", field = "isFollowing")
    public Map<UserForAddRoomChatDTO, Boolean> isFollowing(List<UserForAddRoomChatDTO> users, Principal principal) {

        return batchLoaderHandler.batchLoadFollow(users, UserForAddRoomChatDTO::getUserId, principal);
    }

    @QueryMapping
    public ListResponse<MessageDTO> messages(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument Long chatRoomId,
            @Argument int page,
            @Argument int size) {
        return messageService.getMessages(useDetails.getUser().getId(), chatRoomId, page, size);
    }


    @BatchMapping(typeName = "Message", field = "messageMedias")
    public Map<MessageDTO, List<String>> messageMedias(List<MessageDTO> messages) {
        List<Long> messageIds = messages.stream()
                .map(MessageDTO::getId)
                .toList();

        List<MessageMedia> mediaUrls = messageMediaService.getMediaUrls(messageIds);

        Map<Long, List<String>> map = mediaUrls.stream()
                .collect(Collectors.groupingBy(
                        mm -> mm.getMessage().getId(),
                        Collectors.mapping(ConvertHandler::convertMediaUrl, Collectors.toList())

                ));

        return messages.stream()
                .collect(Collectors.toMap(
                        m -> m,
                        m -> map.getOrDefault(m.getId(), List.of())
                ));
    }

    @BatchMapping(typeName = "Message", field = "user")
    public Map<MessageDTO, UserGQLDTO> user(List<MessageDTO> messages) {

        return batchLoaderHandler.batchLoadUser(messages, MessageDTO::getSenderId);
    }

    @SchemaMapping(typeName = "Message", field = "isMine")
    public Boolean isMine(MessageDTO messages, Principal principal) {
        CustomUserDetails useDetails = userDetailsForBatchMapping.getCustomUserDetails(principal);

        Long myId = useDetails.getUser().getId();

        return myId.equals(messages.getSenderId());
    }
}
