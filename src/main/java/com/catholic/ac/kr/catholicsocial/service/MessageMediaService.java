package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageMediaDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.mapper.MessageMediaMapper;
import com.catholic.ac.kr.catholicsocial.projection.MessageMediaProjection;
import com.catholic.ac.kr.catholicsocial.repository.ChatRoomMemberRepository;
import com.catholic.ac.kr.catholicsocial.repository.MessageMediaRepository;
import com.catholic.ac.kr.catholicsocial.service.hepler.HelperService;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageMediaService {
    private final MessageMediaRepository messageMediaRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final HelperService helperService;

    public List<MessageMedia> getMediaUrls(List<Long> messageIds) {
        return messageMediaRepository.findAllByMessageIds(messageIds);
    }

    public ListResponse<MessageMediaDTO> getAllMediaByChatRoom(Long userId, Long chatRoomId, int page, int size) {
        helperService.validateMember(userId, chatRoomId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<MessageMediaProjection> projections = messageMediaRepository.findAllByChatRoomId(chatRoomId, pageable);

        List<MessageMediaProjection> projectionList = projections.getContent();

        List<MessageMediaDTO> mediaDTOS = MessageMediaMapper.messageMediaDTOList(projectionList);

        return new ListResponse<>(mediaDTOS, new PageInfo(page, size, projections.hasNext()));
    }
}
