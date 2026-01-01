package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.SearchDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.SearchRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.SearchService;
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
public class SearchResolver {
    private final SearchService searchService;

    @QueryMapping
    public ListResponse<SearchDTO> searchHistory(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument int page,
            @Argument int size) {
        return searchService.getAllByUser(useDetails.getUser().getId(), page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> createSearch(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument SearchRequest request) {
        return searchService.createSearch(useDetails.getUser().getId(), request);
    }

    @MutationMapping
    public GraphqlResponse<String> deleteSearch(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long searchId) {
        return searchService.deleteSearch(useDetails.getUser().getId(), searchId);
    }

}
