package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.CommentRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.CommentService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CommentResolver  {
    private final CommentService commentService;
    private final MomentService momentService;

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

    /*
    BatchMapping là DataLoader phiên bản Spring.
    typeName = "CommentDTO" → Đây là resolver cho các trường thuộc type CommentDTO
    field = "moment" → Resolver này sẽ trả dữ liệu cho field moment
     */
    @BatchMapping(typeName = "CommentDTO", field = "moment")
    public Map<CommentDTO, Moment> moment(List<CommentDTO> comments) {

//        System.out.println("======================================================");
//        System.out.println(">>> GRAPHQL BATCH TRIGGERED for Comment.moment");
//        System.out.println(">>> Total comments in batch  = " + comments.size());
//
//        List<Long> momentIds = comments.stream()
//                .map(CommentDTO::getMomentId)
//                .toList();
//
//        System.out.println(">>> Moment IDs requested (raw)      = " + momentIds);

        List<Long> distinctIds = comments.stream()
                .map(CommentDTO::getMomentId)
                .distinct()
                .collect(Collectors.toList());

//        System.out.println(">>> DISTINC Moment IDs (DB fetch)   = " + distinctIds);
//        System.out.println(">>> This should be EXACTLY 1 database query!");
//        System.out.println("======================================================");

        List<Moment> moments = momentService.findAllByIds(distinctIds);

        Map<Long, Moment> momentMap = moments.stream()
                .collect(Collectors.toMap(Moment::getId, Function.identity()));

        return comments.stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> momentMap.get(c.getMomentId())
                ));
    }
}

/*
    Spring sẽ:

    Tìm tất cả các trường GraphQL moment thuộc CommentDTO

    Gom chúng lại thành List<CommentDTO>

    Gọi phương thức bạn viết một lần duy nhất

    Kết quả phải trả về:

    Map<CommentDTO, Moment>


    để Spring biết Moment nào thuộc Comment nào.
     */



    /*
    Map<Long, Moment> momentMap = moments.stream()
        .collect(Collectors.toMap(Moment::getId, Function.identity()));
==> Ý nghĩa: "Chuyển list Moment thành Map (momentId → moment)"
Giả sử DB trả về:

List<Moment> moments = [
    Moment(23),
    Moment(55),
    Moment(71)
];
Sau dòng code trên, bạn sẽ có:

momentMap = {
    23 → Moment(23),
    55 → Moment(55),
    71 → Moment(71)
}
 Giải thích theo từng thành phần
🟦 Moment::getId
Đây là key mapper
→ Lấy id làm key của Map.

Tương đương:

moment -> moment.getId()

Function.identity()
Đây là value mapper
→ Trả về chính object đó.

Tương đương:

moment -> moment
 "identity" nghĩa là không thay đổi object, giữ nguyên.

Nếu không dùng identity() bạn phải viết:

.toMap(moment -> moment.getId(), moment -> moment)
 */

/*
======NOTE=======

Tại sao bọc (wrapper) thì DataLoader hoạt động. Nhưng trả List trực tiếp thì DataLoader KHÔNG hoạt động?
==> Đây là hành vi chuẩn của Spring GraphQL

1. DataLoader chỉ chạy khi GraphQL engine thực sự “đi vào” field resolver

Spring GraphQL trigger DataLoader khi:

Query trả về một object (hoặc list)

Object đó được GraphQL tiếp tục resolve các field con

Trong schema, field con có resolver (hoặc BatchMapping)

Vấn đề xảy ra khi bạn trả về LIST trực tiếp
Khi viết:
@QueryMapping
public List<CommentDTO> getComments(...) { ... }

GraphQL coi đây như là root-level list, không phải một "domain object".
→ GraphQL engine không tạo cấp object wrapper
→ Không kích hoạt BatchMapping cho field con của mỗi item.

Nói cách khác: GraphQL KHÔNG coi List<CommentDTO> là một “GraphQL Object Type” để resolve field theo cơ chế DataLoader.
Nó chỉ coi nó như một danh sách các giá trị đã hoàn thiện.

Nhưng khi bạn dùng wrapper:
@QueryMapping
public CommentListResponse getComments(...) {
    return new CommentListResponse(list);
}

Schema lúc này trở thành:
type Query {
  getComments(...): CommentListResponse
}

type CommentListResponse {
  comments: [CommentDTO]
}

type CommentDTO {
  moment: Moment
}


GraphQL engine sẽ đi theo pipeline chuẩn:
Query → CommentListResponse → comments → CommentDTO → moment → BatchMapping
→ DataLoader được kích hoạt đúng cách.
 */
