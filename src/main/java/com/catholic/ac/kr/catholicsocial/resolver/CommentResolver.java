package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.CommentRequest;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.UserBatchLoader;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.CommentService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
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
public class CommentResolver {
    private final CommentService commentService;
    private final MomentService momentService;
    private final UserBatchLoader userBatchLoader;

    @QueryMapping
    public ListResponse<CommentDTO> getComments(
            @Argument int page,
            @Argument int size,
            @Argument Long momentId) {
        return commentService.getCommentsByMomentId(momentId, page, size);
    }

    @MutationMapping
    public GraphqlResponse<String> createComment(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument Long momentId,
            @Argument CommentRequest request) {
        return commentService.createComment(useDetails.getUser().getId(), momentId, request);
    }

    @BatchMapping(typeName = "CommentDTO", field = "user")
    public Map<CommentDTO, UserGQLDTO> user(List<CommentDTO> comments) {
        System.out.println(">>> BATCH Comment.user triggered");

        List<Long> userIds = comments.stream()
                .map(CommentDTO::getUserId)
                .distinct()
                .toList();

        Map<Long, UserGQLDTO> userMap = userBatchLoader.loadUserByIds(userIds);

        return comments.stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> userMap.get(c.getUserId())
                ));
    }
}

    /*
    BatchMapping là DataLoader phiên bản Spring.
    typeName = "CommentDTO" → Đây là resolver cho các trường thuộc type CommentDTO
    field = "moment" → Resolver này sẽ trả dữ liệu cho field moment
     */
//    @BatchMapping(typeName = "CommentDTO", field = "moment")
//    public Map<CommentDTO, Moment> moment(List<CommentDTO> comments) {
//
//        System.out.println("======================================================");
//        System.out.println(">>> GRAPHQL BATCH TRIGGERED for Comment.moment");
//        System.out.println(">>> Total comments in batch  = " + comments.size());
//
//        List<Long> momentIds = comments.stream()
//                .map(CommentDTO::getMomentId)
//                .toList();
//
//        System.out.println(">>> Moment IDs requested (raw)      = " + momentIds);
//
//        List<Long> distinctIds = comments.stream()
//                .map(CommentDTO::getMomentId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        System.out.println(">>> DISTINC Moment IDs (DB fetch)   = " + distinctIds);
//        System.out.println(">>> This should be EXACTLY 1 database query!");
//        System.out.println("======================================================");
//
//        List<Moment> moments = momentService.findAllByIds(distinctIds);
//
//        Map<Long, Moment> momentMap = moments.stream()
//                .collect(Collectors.toMap(Moment::getId, Function.identity()));
//
//        return comments.stream()
//                .collect(Collectors.toMap(
//                        c -> c,
//                        c -> momentMap.get(c.getMomentId())
//                ));
//    }
