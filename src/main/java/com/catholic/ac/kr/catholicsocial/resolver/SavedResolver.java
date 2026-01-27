package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.MomentGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.SavedDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import com.catholic.ac.kr.catholicsocial.service.SavedService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SavedResolver {
    private final SavedService savedService;
    private final MomentService momentService;
    private final HeartService heartService;

    @QueryMapping
    public ListResponse<SavedDTO> allSaved(
            @AuthenticationPrincipal CustomUserDetails me,
            @Argument int page,
            @Argument int size) {
        return savedService.getAllByUserId(me.getUser().getId(), page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> createSaved(
            @AuthenticationPrincipal CustomUserDetails me,
            @Argument Long momentId) {
        return savedService.save(me.getUser().getId(), momentId);
    }

    @MutationMapping
    public GraphqlResponse<String> deleteSaved(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument Long momentId){
        return savedService.delete(useDetails.getUser().getId(), momentId);
    }

    @BatchMapping(typeName = "SavedDTO", field = "heartCount")
    public Map<SavedDTO, Integer> heartCount(List<SavedDTO> saved) {
        List<Long> momentIds = saved.stream()
                .map(SavedDTO::getMomentId)
                .distinct()
                .toList();

        List<Object[]> objects = heartService.getCountHeart(momentIds); //query của KHÔNG trả về Entity, mà trả về nhiều cột.

        Map<Long, Integer> map = objects.stream()               //Query : SELECT h.moment.id, COUNT(h.id)
                .collect(Collectors.toMap(
                        o -> (Long) o[0],               // o[0] momentId : Lấy momentId làm key
                        o -> ((Long) o[1]).intValue()   // o[1] count: COUNT() trong JPQL trả về Long nhưng heartCount muốn là int
                ));                                             // result:
                                                                //          37 -> 1,
                                                                //          36 -> 4
        return saved.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> map.getOrDefault(s.getMomentId(), 0)
                ));
    }

    @BatchMapping(typeName = "SavedDTO", field = "moment")
    public Map<SavedDTO, MomentGQLDTO> moment(List<SavedDTO> savedList) {
        List<Long> momentIds = savedList.stream()
                .map(SavedDTO::getMomentId)
                .toList();

        System.out.println("momentIds = " + momentIds);

        List<Moment> moments = momentService.getAllByIds(momentIds);

        Map<Long, MomentGQLDTO> map = moments.stream()
                .collect(Collectors.toMap(
                        Moment::getId,
                        ConvertHandler::convertMomentGraphql
                ));

        return savedList.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> map.get(s.getMomentId())
                ));
    }
}
