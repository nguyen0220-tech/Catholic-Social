package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class HeartResolver {
    private final HeartService heartService;

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
}
