package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Active;
import com.catholic.ac.kr.catholicsocial.entity.model.Heart;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.HeartMapper;
import com.catholic.ac.kr.catholicsocial.projection.HeartProjection;
import com.catholic.ac.kr.catholicsocial.repository.ActiveRepository;
import com.catholic.ac.kr.catholicsocial.repository.HeartRepository;
import com.catholic.ac.kr.catholicsocial.repository.MomentRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.ActiveType;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final HeartRepository heartRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final ActiveRepository activeRepository;

    public boolean checkHeart(Long userId, Long momentId) {
        return heartRepository.existsByUser_IdAndMoment_Id(userId, momentId);
    }

    public List<Heart> getAllByMomentIds(List<Long> momentIds) {
        return heartRepository.findAllByMoment_IdIn(momentIds);
    }

    public List<Heart> getAllByIds(List<Long> ids) {
        return heartRepository.findAllById(ids);
    }

    public ListResponse<HeartDTO> getHeartsByMomentId(Long momentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<HeartProjection> hearts = heartRepository.findByMomentId(momentId, pageable);

        return new ListResponse<>(HeartMapper.toListHeartDTO(hearts));
    }

    public GraphqlResponse<String> addHeart(Long userId, Long momentId){
        User user = EntityUtils.getOrThrow(userRepository.findById(userId),"User ");
        Moment moment = EntityUtils.getOrThrow(momentRepository.findById(momentId),"Moment ");

        boolean isHeart = heartRepository.existsByUserAndMoment(user,moment);

        if (isHeart){
            throw new GraphQLException("Heart already exist");
        }

        Heart heart = new Heart();
        heart.setUser(user);
        heart.setMoment(moment);

        heartRepository.save(heart);

        Active newActive = Active.builder()
                .user(user)
                .entityId(heart.getId())
                .type(ActiveType.HEART_MOMENT)
                .build();

        activeRepository.save(newActive);

        return GraphqlResponse.success("Add heart success",null);
    }

    public GraphqlResponse<String> deleteHeart(Long userId, Long momentId){
        User user = EntityUtils.getOrThrow(userRepository.findById(userId),"User ");

        Moment moment = EntityUtils.getOrThrow(momentRepository.findById(momentId),"Moment");

        Heart heart = EntityUtils.getOrThrow(heartRepository.findByUserAndMoment(user,moment),"Heart ");

        heartRepository.delete(heart);

        return GraphqlResponse.success("Delete heart success",null);
    }
}
