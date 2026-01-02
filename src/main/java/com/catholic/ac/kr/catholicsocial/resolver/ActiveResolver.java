package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.model.Comment;
import com.catholic.ac.kr.catholicsocial.entity.model.Heart;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.UserBatchLoader;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.ActiveService;
import com.catholic.ac.kr.catholicsocial.service.CommentService;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ActiveResolver {
    private final ActiveService activeService;
    private final MomentService momentService;
    private final HeartService heartService;
    private final CommentService commentService;
    private final UserBatchLoader userBatchLoader;

    @QueryMapping
    public ListResponse<ActiveDTO> allActive(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Argument int page,
            @Argument int size) {
        System.out.println("current user: " + useDetails.getUsername());
        System.out.println("List active"+ activeService.getAllByUserId(useDetails.getUser().getId(), page, size));
        return activeService.getAllByUserId(useDetails.getUser().getId(), page, size);
    }

    @BatchMapping(typeName = "ActiveDTO", field = "user")
    public Map<ActiveDTO, UserGQLDTO> user(List<ActiveDTO> actives) {
        List<Long> userIds = actives.stream()
                .map(ActiveDTO::getUserId)
                .distinct()
                .toList();

        System.out.println("BatchMapping userIds" + userIds);

        Map<Long, UserGQLDTO> userGQLDTOMap = userBatchLoader.loadUserByIds(userIds);

        return actives.stream()
                .collect(Collectors.toMap(
                        a -> a,
                        a -> userGQLDTOMap.get(a.getUserId())
                ));
    }

    @BatchMapping(typeName = "ActiveDTO", field = "target")
    public Map<ActiveDTO, Object> target(List<ActiveDTO> actives) {
        Map<String, List<Long>> idsByType = actives.stream()
                .collect(Collectors.groupingBy(
                        ActiveDTO::getType,
                        Collectors.mapping(ActiveDTO::getEntityId, Collectors.toList())
                ));

        List<Long> momentIds = idsByType.getOrDefault("UPLOAD_MOMENT", List.of());
        List<Long> commentIds = idsByType.getOrDefault("COMMENT_MOMENT", List.of());
        List<Long> heartIds = idsByType.getOrDefault("HEART_MOMENT", List.of());

        List<Moment> moments = momentService.getAllByIds(momentIds);
        List<Comment> comments = commentService.getCommentsByIds(commentIds);
        List<Heart> hearts = heartService.getAllByIds(heartIds);

        Map<Long, MomentUserDTO> momentUserDTOMap = moments.stream()
                .collect(Collectors.toMap(
                        Moment::getId,
                        ConvertHandler::convertMomentUserDTO)
                );

        Map<Long, CommentDTO> commentDTOMap = comments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        ConvertHandler::convertToCommentDTO
                ));

        Map<Long, HeartDTO> heartDTOMap = hearts.stream()
                .collect(Collectors.toMap(
                        Heart::getId
                        , ConvertHandler::convertToHeartDTO
                ));

        //debug
        log.info("momentIds: {}", momentIds);
        log.info("commentIds: {}", commentIds);
        log.info("heartIds: {}", heartIds);
        log.info("momentDtoMap key: {}", momentUserDTOMap.keySet());
        log.info("commentDtoMap key: {}", commentDTOMap.keySet());
        log.info("heartDtoMap key: {}", heartDTOMap.keySet());

        Map<ActiveDTO, Object> result = new ConcurrentHashMap<>();

        // Filter trước –> luôn trả object
        for (ActiveDTO a : actives) {
            System.out.println("Active id=" + a.getId()
                    + " type=" + a.getType()
                    + " entityId=" + a.getEntityId());
            Object value = switch (a.getType()) {
                case "UPLOAD_MOMENT" -> momentUserDTOMap.get(a.getEntityId());
                case "COMMENT_MOMENT" -> commentDTOMap.get(a.getEntityId());
                case "HEART_MOMENT" -> heartDTOMap.get(a.getEntityId());
                default -> null;
            };

            if (value != null) {
                result.put(a, value);
            }
        }

        return result;
    }
}

/*
===UNION DataLoader: batch theo TYPE + ID===
Những BatchMapping đã viết cho Moment / Comment / Heart là ĐỦ và ĐÚNG
 Nhưng:
 ActiveDTO.target là một điểm batch KHÁC
 Batch theo target = batch để LẤY OBJECT GỐC, không phải batch field con
=> Hai khái niệm batch này KHÔNG trùng nhau

vấn đề:
Với 50 activity:
50 lần gọi getMoment / getComment / getHeart
Dù bên trong đã có batch cho field con

    @SchemaMapping(typeName = "ActiveDTO", field = "target")
    public Object target(ActiveDTO activeDTO) {
        if ("UPLOAD_MOMENT".equals(activeDTO.getType())) {
            Moment moment = momentService.getMoment(activeDTO.getEntityId());
            return ConvertHandler.convertMomentUserDTO(moment);

        } else if ("COMMENT_MOMENT".equals(activeDTO.getType())) {
            Comment comment = commentService.getComment(activeDTO.getEntityId());
            return ConvertHandler.convertToCommentDTO(comment);

        } else if ("HEART_MOMENT".equals(activeDTO.getType())) {
            Heart heart = heartService.getHeart(activeDTO.getEntityId());
            return ConvertHandler.convertToHeartDTO(heart);
        }

        return null;
    }
 */
