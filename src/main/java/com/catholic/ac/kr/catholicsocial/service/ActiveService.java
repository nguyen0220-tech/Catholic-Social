package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.dto.ActiveDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.mapper.ActiveMapper;
import com.catholic.ac.kr.catholicsocial.projection.ActiveProjection;
import com.catholic.ac.kr.catholicsocial.repository.ActiveRepository;
import com.catholic.ac.kr.catholicsocial.status.ActiveType;
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

    public ListResponse<ActiveDTO> getAllByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ActiveProjection> activeProjections = activeRepository.findAllByUserId(userId, pageable);

        List<ActiveProjection> activeList = activeProjections.getContent();

        List<ActiveDTO> activeDTOS = ActiveMapper.toActiveDTO(activeList);

        return new ListResponse<>(activeDTOS, new PageInfo(page, size, activeProjections.hasNext()));
    }

    public ListResponse<ActiveDTO> getAndFilterAllByUserId(Long userId, ActiveType activeType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ActiveProjection> projections = activeRepository.findAllByUserIdAndType(userId, activeType, pageable);

        List<ActiveProjection> activeList = projections.getContent();

        List<ActiveDTO> activeDTOS = ActiveMapper.toActiveDTO(activeList);

        return new ListResponse<>(activeDTOS, new PageInfo(page, size, projections.hasNext()));
    }
}
