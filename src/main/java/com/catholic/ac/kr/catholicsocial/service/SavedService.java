package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.service.hepler.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.dto.SavedDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.Saved;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.SavedMapper;
import com.catholic.ac.kr.catholicsocial.projection.SavedProjection;
import com.catholic.ac.kr.catholicsocial.repository.MomentRepository;
import com.catholic.ac.kr.catholicsocial.repository.SavedRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedService {
    private final SavedRepository savedRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;

    public List<Long> getAllByMomentIds(Long userId, List<Long> momentIds) {
        return savedRepository.findAllByMomentIds(userId, momentIds);
    }

    public ListResponse<SavedDTO> getAllByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<SavedProjection> projectionPage = savedRepository.findAllByUserId(userId, pageable);

        List<SavedProjection> projections = projectionPage.getContent();

        List<SavedDTO> savedDTOS = SavedMapper.toSavedDTO(projections);

        return new ListResponse<>(savedDTOS, new PageInfo(page, size, projectionPage.hasNext()));
    }

    public GraphqlResponse<String> save(Long userId, Long momentId) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        Moment moment = EntityUtils.getOrThrow(momentRepository.findById(momentId), "Moment");

        boolean exists = savedRepository.existsByUserAndMoment(user, moment);

        if (exists) {
            throw new GraphQLException("Saved already exists");
        }

        Saved newSaved = new Saved();
        newSaved.setUser(user);
        newSaved.setMoment(moment);

        savedRepository.save(newSaved);

        return GraphqlResponse.success("Saved moment success", null);
    }

    public GraphqlResponse<String> delete(Long userId, Long momentId) {
        Saved saved = savedRepository.findByUser_IdAndMoment_Id(userId, momentId)
                .orElseThrow(() -> new GraphQLException("Saved not found"));

        savedRepository.delete(saved);

        return GraphqlResponse.success("Deleted success", null);
    }
}
