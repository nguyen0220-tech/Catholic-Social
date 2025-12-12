package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Heart;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.HeartMapper;
import com.catholic.ac.kr.catholicsocial.repository.HeartRepository;
import com.catholic.ac.kr.catholicsocial.repository.MomentRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
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

    public ListResponse<HeartDTO> getHeartsByMomentId(Long momentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Heart> hearts = heartRepository.findByMomentId(momentId, pageable);

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
