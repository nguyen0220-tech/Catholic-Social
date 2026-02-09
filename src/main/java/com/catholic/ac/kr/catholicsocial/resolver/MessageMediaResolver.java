package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageMediaDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.BatchLoaderHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.MessageMediaService;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MessageMediaResolver {

    private final MessageMediaService messageMediaService;
    private final BatchLoaderHandler batchLoaderHandler;

    @QueryMapping
    public ListResponse<MessageMediaDTO> messageMedias(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Argument Long chatRoomId,
            @Argument int page,
            @Argument int size
    ) {
        return messageMediaService.getAllMediaByChatRoom(userDetails.getUser().getId(), chatRoomId, page, size);
    }

    @BatchMapping(typeName = "MessageMedia", field = "user")
    public Map<MessageMediaDTO, UserGQLDTO> user(List<MessageMediaDTO> medias) {
        return batchLoaderHandler.batchLoadUser(medias, MessageMediaDTO::getSenderId);
    }
}
