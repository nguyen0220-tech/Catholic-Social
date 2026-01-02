package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.UserBatchLoader;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HeartResolver {
    private final HeartService heartService;
    private final UserBatchLoader userBatchLoader;
    private final MomentService momentService;

    @QueryMapping
    public ListResponse<HeartDTO> hearts(
            @Argument Long momentId,
            @Argument int page,
            @Argument int size) {
        return heartService.getHeartsByMomentId(momentId, page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> addHeart(@AuthenticationPrincipal CustomUseDetails useDetails, @Argument Long momentId) {
        return heartService.addHeart(useDetails.getUser().getId(), momentId);
    }

    @MutationMapping
    public GraphqlResponse<String> deleteHeart(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long momentId) {
        return heartService.deleteHeart(useDetails.getUser().getId(), momentId);
    }

    @SchemaMapping(typeName = "HeartDTO", field = "moment")
    public MomentGQLDTO moment(HeartDTO heartDTO) {
        Long momentId = heartDTO.getMomentId();

        if (momentId == null) {
            System.out.println("MomentId is null ~~~");
            return null;
        }

        Moment moment = momentService.getMoment(momentId);

        return ConvertHandler.convertMomentGraphql(moment);
    }

    @BatchMapping(typeName = "HeartDTO", field = "user")
    public Map<HeartDTO, UserGQLDTO> user(List<HeartDTO> hearts) {
        List<Long> userIds = hearts.stream()
                .map(HeartDTO::getUserId)
                .distinct()
                .toList();

        log.info(">>> BATCH ids: {}", userIds);

        Map<Long, UserGQLDTO> userGQLDTOMap = userBatchLoader.loadUserByIds(userIds);

        return hearts.stream()
                .collect(Collectors.toMap(
                        h -> h,
                        h -> userGQLDTOMap.get(h.getUserId())
                ));
    }
}
