package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.model.Comment;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.resolver.batchloader.BatchLoaderHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.*;
import com.catholic.ac.kr.catholicsocial.status.NotifyType;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class NotificationResolver {
    private final NotificationService notificationService;
    private final UserService userService;
    private final MomentService momentService;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;
    private final FollowService followService;
    private final CommentService commentService;
    private final BatchLoaderHandler batchLoaderHandler;

    @QueryMapping
    public ListResponse<NotificationDTO> notifications(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument int page,
            @Argument int size) {
        return notificationService.getAllNotifications(useDetails.getUser().getId(), page, size);
    }

    @QueryMapping
    public int unreadCount(@AuthenticationPrincipal CustomUserDetails useDetails) {
        return notificationService.getCountNotifications(useDetails.getUser().getId());
    }

    @MutationMapping
    public GraphqlResponse<String> maskAsRead(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument Long notificationId) {
        return notificationService.maskAsRead(useDetails.getUser().getId(), notificationId);
    }

    @MutationMapping
    public GraphqlResponse<String> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Argument Long notificationId) {
        return notificationService.deleteNotification(useDetails.getUser().getId(), notificationId);
    }

    @BatchMapping(typeName = "NotificationDTO", field = "actor")
    public Map<NotificationDTO, UserGQLDTO> actor(List<NotificationDTO> notifications) {

        return batchLoaderHandler.batchLoadUser(notifications, NotificationDTO::getActorId);
    }

    @BatchMapping(typeName = "NotificationDTO", field = "target")
    public Map<NotificationDTO, Object> target(List<NotificationDTO> notifications) {
        Map<String, List<Long>> idsByType = notifications.stream()
                .collect(Collectors.groupingBy(
                        NotificationDTO::getType,
                        Collectors.mapping(NotificationDTO::getEntityId, Collectors.toList())
                ));

        List<Long> userIds = idsByType.getOrDefault(NotifyType.FOLLOW.name(), List.of());
        List<Long> commentIds = idsByType.getOrDefault(NotifyType.COMMENT_MOMENT.name(), List.of());
        List<Long> momentIds = idsByType.getOrDefault(NotifyType.HEART_MOMENT.name(), List.of());

        List<User> users = userService.getAllById(userIds);
        List<Comment> comments = commentService.getCommentsByIds(commentIds);
        List<Moment> moments = getMoments(momentIds);

        Map<Long, NotificationFollowerDTO> notificationFollowerDTOMap = users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        ConvertHandler::convertToNotificationFollowerDTO

                ));

        Map<Long, NotificationCommentDTO> notificationCommentDTOMap = comments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        ConvertHandler::convertToNotificationCommentDTO
                ));

        Map<Long, MomentGQLDTO> momentGQLDTOMap = moments.stream()
                .collect(Collectors.toMap(
                        Moment::getId,
                        ConvertHandler::convertMomentGraphql
                ));


        Map<NotificationDTO, Object> result = new HashMap<>();

        for (NotificationDTO n : notifications) {
            Object value = switch (n.getType()) {
                case "FOLLOW" -> notificationFollowerDTOMap.get(n.getEntityId());
                case "COMMENT_MOMENT" -> notificationCommentDTOMap.get(n.getEntityId());
                case "HEART_MOMENT" -> momentGQLDTOMap.get(n.getEntityId());
                default -> null;
            };

            result.put(n, value);
        }
        return result;
    }

    @BatchMapping
    public Map<NotificationFollowerDTO, Boolean> reverseFollowed(
            List<NotificationFollowerDTO> followers,
            Principal principal) {

        return batchLoaderHandler.batchLoadFollow(followers,NotificationFollowerDTO::getFollowerId, principal);
    }

    @BatchMapping
    public Map<NotificationCommentDTO, MomentGQLDTO> moment(List<NotificationCommentDTO> comments) {
        List<Long> momentIds = comments.stream()
                .map(NotificationCommentDTO::getMomentId)
                .distinct()
                .toList();

        List<Moment> moments = getMoments(momentIds);

        Map<Long, MomentGQLDTO> map = moments.stream()
                .collect(Collectors.toMap(
                        Moment::getId,
                        ConvertHandler::convertMomentGraphql
                ));

        return comments.stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> map.get(c.getMomentId())
                ));
    }

    private List<Moment> getMoments(List<Long> momentIds) {
        return momentService.getAllByIds(momentIds);
    }
}
