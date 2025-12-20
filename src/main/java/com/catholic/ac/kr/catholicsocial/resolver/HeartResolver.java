package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.UserBatchLoader;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HeartResolver {
    private final HeartService heartService;
    private final UserBatchLoader userBatchLoader;

    @QueryMapping
    public ListResponse<HeartDTO> getHeartsByMomentId(
            @Argument Long momentId,
            @Argument int page,
            @Argument int size ){
        return heartService.getHeartsByMomentId(momentId, page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> addHeart(@AuthenticationPrincipal CustomUseDetails useDetails, @Argument Long momentId){
        return heartService.addHeart(useDetails.getUser().getId(), momentId);
    }

    @MutationMapping
    public GraphqlResponse<String> deleteHeart(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long momentId){
        return heartService.deleteHeart(useDetails.getUser().getId(), momentId);
    }

    @BatchMapping(typeName = "HeartDTO",field = "user")
    public Map<HeartDTO, UserGQLDTO> user(List<HeartDTO> hearts){
        List<Long> userIds = hearts.stream()
                .map(HeartDTO::getUserId)
                .distinct()
                .toList();

        System.out.println("BATCH ids: " + userIds);

        Map<Long, UserGQLDTO> userGQLDTOMap = userBatchLoader.loadUserByIds(userIds);

        return hearts.stream()
                .collect(Collectors.toMap(
                        h ->h,
                        h -> userGQLDTOMap.get(h.getUserId())
                ));
    }
}
