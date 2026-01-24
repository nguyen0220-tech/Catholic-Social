package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.repository.MessageMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageMediaService {
    private final MessageMediaRepository messageMediaRepository;

    public List<MessageMedia> getMediaUrls(List<Long> messageIds) {
        return messageMediaRepository.findAllByMessageIds(messageIds);
    }
}
