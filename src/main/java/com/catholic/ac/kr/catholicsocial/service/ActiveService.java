package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ActiveDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ActiveMapper;
import com.catholic.ac.kr.catholicsocial.projection.ActiveProjection;
import com.catholic.ac.kr.catholicsocial.repository.ActiveRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
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
public class ActiveService {
    private final ActiveRepository activeRepository;
    private final UserRepository userRepository;

    public ListResponse<ActiveDTO> getAllByUser(Long userId, int page, int size) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId),"User ");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ActiveProjection> activeProjections = activeRepository.findAllByUser(user, pageable);

        List<ActiveProjection> activeList = activeProjections.getContent();

        List<ActiveDTO> activeDTOS = ActiveMapper.toActiveDTO(activeList);

        return new ListResponse<>(activeDTOS, new PageInfo(page, size, activeProjections.hasNext()));
    }
}
