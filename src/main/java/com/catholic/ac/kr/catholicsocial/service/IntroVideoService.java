package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.service.hepler.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.IntroVideoRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.IntroVideo;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.exception.ResourceNotFoundException;
import com.catholic.ac.kr.catholicsocial.mapper.IntroVideoMapper;
import com.catholic.ac.kr.catholicsocial.projection.IntroProjection;
import com.catholic.ac.kr.catholicsocial.repository.IntroVideoRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.IntroStatus;
import com.catholic.ac.kr.catholicsocial.uploadfile.UploadFileHandler;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntroVideoService {
    private final IntroVideoRepository introVideoRepository;
    private final UserRepository userRepository;
    private final UploadFileHandler uploadFileHandler;
    private final Cloudinary cloudinary;

    public List<IntroVideo> getAllByIds(List<Long> ids) {
        return introVideoRepository.findAllById(ids);
    }

    public IntroVideoDTO getIntroVideo(Long userId) {
        Optional<IntroVideoDTO> introVideo = introVideoRepository.findByUserIdAndStatus(userId, IntroStatus.ACTIVE);

        if (introVideo.isPresent()) {
            if (introVideo.get().getExp().isAfter(LocalDateTime.now())) {
                return introVideo.get();
            }
        }
        return null;
    }

    public ListResponse<IntroVideoDTO> getIntrosCanRestore(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("exp").descending());

        Page<IntroProjection> introVideoPage = introVideoRepository
                .findAllIntroCanRestore(userId, LocalDateTime.now(), IntroStatus.REMOVED, pageable);

        List<IntroProjection> introVideos = introVideoPage.getContent();

        return new ListResponse<>(IntroVideoMapper.toIntroVideoDTOList(introVideos), new PageInfo(page, size, introVideoPage.hasNext()));
    }

    public ApiResponse<IntroVideoDTO> uploadIntro(Long userId, IntroVideoRequest request) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        Optional<IntroVideo> optionalIntroVideo = introVideoRepository.findByUser_IdAndStatus(userId, IntroStatus.ACTIVE);

        optionalIntroVideo.ifPresent(video -> {
            video.setStatus(IntroStatus.REMOVED);
            introVideoRepository.save(video);
        });

        IntroVideo newIntro = new IntroVideo();
        newIntro.setUser(user);
        newIntro.setExp(LocalDateTime.now().plusDays(request.getExpDay()));

        Map<?, ?> intro = uploadFileHandler.uploadVideo(userId, request.getIntro());

        newIntro.setUrl(intro.get("url").toString());
        newIntro.setPublicId(intro.get("public_id").toString());

        introVideoRepository.save(newIntro);


        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "upload intro success",
                IntroVideoMapper.toIntroVideoDTO(newIntro));

    }

    public ApiResponse<String> removeIntro(Long userId, Long introId) {
        IntroVideo introVideo = introVideoRepository.findByIdAndUser_Id(introId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("introVideo not found"));
        introVideo.setStatus(IntroStatus.REMOVED);

        introVideoRepository.save(introVideo);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "remove intro video successfully");
    }

    public ApiResponse<String> restoreIntro(Long userId, Long introId) {
        IntroVideo introToRestore = introVideoRepository.findByIdAndUser_Id(introId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("introVideo not found"));

        if (introToRestore.getStatus() == (IntroStatus.ACTIVE) ||
                introToRestore.getExp().isEqual(LocalDateTime.now())) {
            throw new IllegalStateException("Intro video is not restorable"); //object đang ở trạng thái không hợp lệ để thực hiện hành động này
        }
        List<IntroVideo> updateList = new ArrayList<>();

        Optional<IntroVideo> introActOpt = introVideoRepository.findByUser_IdAndStatus(userId, IntroStatus.ACTIVE);

        introActOpt.ifPresent(introAct -> {
            introAct.setStatus(IntroStatus.REMOVED);
            updateList.add(introAct);

        });

        updateList.add(introToRestore);
        introToRestore.setStatus(IntroStatus.ACTIVE);

        introVideoRepository.saveAll(updateList);


        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "restore intro video successfully");
    }

    public void clearIntroExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<IntroVideo> list = introVideoRepository.findIntroVideoExpired(now);
        list.forEach(intro -> {
            String publicId = intro.getPublicId();

            try {
                cloudinary.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap(
                                "resource_type", "video"
                        )
                );
            } catch (Exception e) {
                log.error("Error occurred while deleting video", e);
            }
        });

        introVideoRepository.deleteAll(list);
    }
}
