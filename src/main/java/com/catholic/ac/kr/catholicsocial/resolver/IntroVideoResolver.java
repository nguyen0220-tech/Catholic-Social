package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.IntroContentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.IntroVideo;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.IntroVideoService;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class IntroVideoResolver {
    private final IntroVideoService introVideoService;

    @QueryMapping
    public ListResponse<IntroVideoDTO> allVideosRestore(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument int page,
            @Argument int size) {
        return introVideoService.getIntrosCanRestore(useDetails.getUser().getId(), page, size);
    }

    @BatchMapping(typeName = "IntroDetail", field = "content")
    public Map<IntroVideoDTO, IntroContentDTO> content(List<IntroVideoDTO> introVideos) {
        List<Long> ids = introVideos.stream()
                .map(IntroVideoDTO::getId)
                .distinct()
                .toList();

        List<IntroVideo> introList = introVideoService.getAllByIds(ids);

        Map<Long, IntroContentDTO> map = introList.stream()
                .collect(Collectors.toMap(
                        IntroVideo::getId,
                        ConvertHandler::convertToIntroContentDTO

                ));

        return introVideos.stream()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> map.get(i.getId())
                ));
    }
}
