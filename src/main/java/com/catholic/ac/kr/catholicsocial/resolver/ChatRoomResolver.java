package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.AddMemberRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.GroupChatRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UpdateChatRoomRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.BatchLoaderHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.ChatRoomService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatRoomResolver {
    private final ChatRoomService chatRoomService;
    private final BatchLoaderHandler batchLoaderHandler;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;

    @QueryMapping
    public ListResponse<ChatRoomDTO> roomsChat(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument int page,
            @Argument int size) {
        return chatRoomService.getChatRooms(useDetails.getUser().getId(), page, size);
    }

    @QueryMapping
    public List<UserRecentMessageDTO> usersRecentMessage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatRoomService.getUsersRecentMessage(userDetails.getUser().getId());
    }

    @BatchMapping(typeName = "UserRecentMessage", field = "user")
    public Map<UserRecentMessageDTO, UserGQLDTO> userRecent(List<UserRecentMessageDTO> users) {
        return batchLoaderHandler.batchLoadUser(users, UserRecentMessageDTO::getId);
    }

    @BatchMapping(typeName = "UserRecentMessage",field = "hasRoom")
    public Map<UserRecentMessageDTO, ChatRoomDTO> hasRoomForRecent(
            List<UserRecentMessageDTO> recipients
            , Principal principal
    ){

        CustomUserDetails userDetails = userDetailsForBatchMapping.getCustomUserDetails(principal);

        List<Long> userIds = recipients.stream()
                .map(UserRecentMessageDTO::getId)
                .toList();

        Map<Long, ChatRoom> map = chatRoomService
                .getAllChatRoomWithUsers(userIds, userDetails.getUser().getId());

        Map<UserRecentMessageDTO, ChatRoomDTO> result = new HashMap<>();

        for (UserRecentMessageDTO user : recipients) {
            ChatRoom chatRoom = map.get(user.getId());
            result.put(
                    user,
                    chatRoom != null ? ConvertHandler.convertToChatRoomDTO(chatRoom) : null
            );
        }

        return result;
    }

    @QueryMapping
    public ListResponse<UserForCreateRoomChatDTO> usersForCreateRoomChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument String keyword,
            @Argument int page,
            @Argument int size
    ) {
        return chatRoomService.getUsersForCreateRoom(userDetails.getUser().getId(), keyword, page, size);
    }

    @BatchMapping(typeName = "UserForCreateChatRoom", field = "user")
    public Map<UserForCreateRoomChatDTO, UserGQLDTO> usersForCreate(List<UserForCreateRoomChatDTO> users) {
        return batchLoaderHandler.batchLoadUser(users, UserForCreateRoomChatDTO::getUserId);
    }

    @BatchMapping(typeName = "UserForCreateChatRoom", field = "isFollowing")
    public Map<UserForCreateRoomChatDTO, Boolean> isFollow(List<UserForCreateRoomChatDTO> users, Principal principal) {
        return batchLoaderHandler.batchLoadFollow(users, UserForCreateRoomChatDTO::getUserId, principal);
    }


    @BatchMapping(typeName = "UserForCreateChatRoom", field = "hasRoom")
    public Map<UserForCreateRoomChatDTO, ChatRoomDTO> hasRoom(
            List<UserForCreateRoomChatDTO> repicientList,
            Principal principal) {

        CustomUserDetails userDetails = userDetailsForBatchMapping.getCustomUserDetails(principal);

        List<Long> userIds = repicientList.stream()
                .map(UserForCreateRoomChatDTO::getUserId)
                .toList();

        Map<Long, ChatRoom> map = chatRoomService
                .getAllChatRoomWithUsers(userIds, userDetails.getUser().getId());

        Map<UserForCreateRoomChatDTO, ChatRoomDTO> result = new HashMap<>();

        for (UserForCreateRoomChatDTO user : repicientList) {
            ChatRoom chatRoom = map.get(user.getUserId());
            result.put(
                    user,
                    chatRoom != null ? ConvertHandler.convertToChatRoomDTO(chatRoom) : null
            );
        }

        return result;
    }

    @MutationMapping
    public GraphqlResponse<RoomChatDTO> createGroupChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument GroupChatRequest request
    ) {
        return chatRoomService.createGroupChat(userDetails.getUser().getId(), request);
    }

    @MutationMapping
    public GraphqlResponse<RoomChatDTO> updateChatRoom(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument UpdateChatRoomRequest request
    ) {
        return chatRoomService.updateChatRoom(useDetails.getUser().getId(), request);
    }

    @BatchMapping(typeName = "ChatRoom", field = "detail")
    public Map<ChatRoomDTO, ChatRoomDetailDTO> detail(List<ChatRoomDTO> rooms) {
        List<Long> roomIds = rooms.stream()
                .map(ChatRoomDTO::getChatRoomId)
                .distinct()
                .toList();

        List<ChatRoom> chatRooms = chatRoomService.getAllChatRoomsByIds(roomIds);

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

        List<ChatRoomMember> allMembers = chatRoomService.getAllChatRoomMembers(roomIds);

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument Long chatRoomId,
            @Argument String keyword,
            @Argument int page,
            @Argument int size
    ) {
        return chatRoomService.getUserForAddRoomChat(userDetails.getUser().getId(), chatRoomId, keyword, page, size);
    }


    @BatchMapping(typeName = "UserForAddChatRoom", field = "userForChatRoom")
    public Map<UserForAddRoomChatDTO, UserGQLDTO> userForChatRoom(List<UserForAddRoomChatDTO> users) {

        return batchLoaderHandler.batchLoadUser(users, UserForAddRoomChatDTO::getUserId);
    }

    @BatchMapping(typeName = "UserForAddChatRoom", field = "isFollowing")
    public Map<UserForAddRoomChatDTO, Boolean> isFollowing(List<UserForAddRoomChatDTO> users, Principal principal) {

        return batchLoaderHandler.batchLoadFollow(users, UserForAddRoomChatDTO::getUserId, principal);
    }

    @MutationMapping
    public GraphqlResponse<String> addMemberForChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument AddMemberRequest request
    ) {
        return chatRoomService.addMemberToChatRoom(userDetails.getUser().getId(), request);
    }

    // ====Members of Chat Room
    @QueryMapping
    public ListResponse<MemberOfChatRoomDTO> members(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument Long chatRoomId,
            @Argument int page,
            @Argument int size
    ) {
        return chatRoomService.getMembersOfChatRoom(userDetails.getUser().getId(), chatRoomId, page, size);
    }

    @BatchMapping(typeName = "MemberOfChatRoom", field = "user")
    public Map<MemberOfChatRoomDTO, UserGQLDTO> users(List<MemberOfChatRoomDTO> members) {

        return batchLoaderHandler.batchLoadUser(members, MemberOfChatRoomDTO::getUserId);
    }

    @MutationMapping
    public GraphqlResponse<String> leaveChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument Long chatRoomId
    ) {
        return chatRoomService.leaveChatRoom(userDetails.getUser().getId(), chatRoomId);
    }


}
