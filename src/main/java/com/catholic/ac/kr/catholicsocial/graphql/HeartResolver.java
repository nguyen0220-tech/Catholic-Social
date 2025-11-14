package com.catholic.ac.kr.catholicsocial.graphql;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HeartResolver {
    private final HeartService heartService;

    @QueryMapping
    public ApiResponse<List<HeartDTO>> getHeartsByMomentId(
            @Argument Long momentId,
            @Argument int page,
            @Argument int size ){
        return heartService.getHeartsByMomentId(momentId, page, size);
    }

    @MutationMapping
    public ApiResponse<String> addHeart(@AuthenticationPrincipal CustomUseDetails useDetails, @Argument Long momentId){
        return heartService.addHeart(useDetails.getUser().getId(), momentId);
    }

    @MutationMapping
    public ApiResponse<String> deleteHeart(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long momentId){
        return heartService.deleteHeart(useDetails.getUser().getId(), momentId);
    }
}
