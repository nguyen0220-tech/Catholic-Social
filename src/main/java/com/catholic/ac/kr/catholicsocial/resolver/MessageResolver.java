package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.BatchLoaderHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.MessageMediaService;
import com.catholic.ac.kr.catholicsocial.service.MessageService;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MessageResolver {
    private final MessageService messageService;
    private final MessageMediaService messageMediaService;
    private final BatchLoaderHandler batchLoaderHandler;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;


    // ====Message=====
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
