package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.CommentRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentResolver  {
    private final CommentService commentService;

    @QueryMapping
    public List<CommentDTO> getComments(
            @Argument int page,
            @Argument int size,
            @Argument Long momentId) {
        return commentService.getCommentsByMomentId(momentId, page, size);
    }

    @MutationMapping
    public String createComment(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long momentId,
            @Argument CommentRequest request) {
        return commentService.createComment(useDetails.getUser().getId(), momentId, request);
    }
}
