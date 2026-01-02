package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDetailDTO;
import com.catholic.ac.kr.catholicsocial.projection.MomentProjection;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MomentRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MomentUpdateRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Active;
import com.catholic.ac.kr.catholicsocial.entity.model.Image;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.MomentMapper;
import com.catholic.ac.kr.catholicsocial.repository.ActiveRepository;
import com.catholic.ac.kr.catholicsocial.repository.MomentRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.ActiveType;
import com.catholic.ac.kr.catholicsocial.status.ImageType;
import com.catholic.ac.kr.catholicsocial.uploadfile.UploadFileHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MomentService {
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    private final UploadFileHandler uploadFileHandler;
    private final ActiveRepository activeRepository;

    public Moment getMoment(Long id) {
        return EntityUtils.getOrThrow(momentRepository.findById(id),"Moment");
    }

    public MomentDetailDTO getMomentDetail(Long id) {
        MomentProjection projection = momentRepository.findByMomentId(id);

        return MomentMapper.toMomentDetailDTO(projection);
    }

    //use for DataLoader
    public List<Moment> getAllByIds(List<Long> ids) {
        return momentRepository.findAllById(ids);
    }

    public List<Moment> findMomentsByUserId(Long userId, int page, int size) {
        return momentRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public List<Moment> findAllByUserIds(List<Long> userIds) {
        return momentRepository.findByUser_IdIn(userIds);
    }

    public ApiResponse<List<MomentDTO>> getAllMoments(Long userId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<Moment> moments = momentRepository.findAllMomentFollowAndPublic(userId,pageable);

        List<MomentDTO> momentDTOS = MomentMapper.toListDTO(moments);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Get all moments", momentDTOS);    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<List<MomentDTO>> getAllMomentsByUserId(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());

        List<Moment> moments = momentRepository.findByUserId(userId,pageable);

        List<MomentDTO> momentDTOS = MomentMapper.toListDTO(moments);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Get all moments", momentDTOS);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> uploadMoment(Long userId, MomentRequest request) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User ");

        if ((request.getContent() == null || request.getContent().isBlank())
                && (request.getFiles() == null || request.getFiles().isEmpty())) {
            return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Content or File required");
        }

        Moment moment = new Moment();
        moment.setUser(user);
        moment.setContent(request.getContent());

        if (!request.getFiles().isEmpty()) {
            List<Image> images = new ArrayList<>();
            for (MultipartFile file : request.getFiles()) {
                Image image = new Image();
                image.setUser(user);
                image.setType(ImageType.MOMENT);
                image.setImageUrl(uploadFileHandler.uploadFile(user.getId(), file));

                images.add(image);
            }

            moment.setListImage(images);
        }

        moment.setShare(request.getShare());

        momentRepository.save(moment);

        Active newActive = Active.builder()
                .user(user)
                .entityId(moment.getId())
                .type(ActiveType.UPLOAD_MOMENT)
                .build();

        activeRepository.save(newActive);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Uploaded moment successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> updateMoment(Long userId, MomentUpdateRequest request) {
        Moment moment = getMoment(userId,request.getMomentId());

        moment.setContent(request.getContent());
        moment.setShare(request.getShare());

        momentRepository.save(moment);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Updated moment success");
    }

    public ApiResponse<String> deleteMoment(Long userId, Long momentId) {
        Moment moment = getMoment(userId,momentId);
        momentRepository.delete(moment);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Deleted moment success");

    }

    private Moment getMoment(Long userId, Long momentId) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User ");

        return EntityUtils.getOrThrow(momentRepository
                .findByIdAndUser(momentId, user), "Moment");
    }


}
