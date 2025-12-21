package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.model.Image;
import com.catholic.ac.kr.catholicsocial.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public List<Image> getImages(List<Long> ids) {
        return imageRepository.findAllByMoment_IdIn(ids);
    }
}
