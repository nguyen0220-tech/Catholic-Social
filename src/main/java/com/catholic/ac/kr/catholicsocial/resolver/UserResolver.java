package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.model.*;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;
    private final ImageService imageService;
    private final CommentService commentService;
    private final MomentService momentService;
    private final HeartService heartService;
    private final FollowService followService;

    @QueryMapping
    public UserProfileDTO getProfile(@Argument Long userId) {
        return userService.getUserProfileDTO(userId);
    }

    @SchemaMapping(typeName = "UserProfileDTO", field = "isFollowing")
    public boolean isFollowing(
            UserProfileDTO user,
            @AuthenticationPrincipal CustomUseDetails me) {
        if (me == null) return false;

        return followService.isFollowing(me.getUser().getId(), user.getId());
    }

    @SchemaMapping(typeName = "UserProfileDTO", field = "isBlocked")
    public boolean isBlocked(
            UserProfileDTO user,
            @AuthenticationPrincipal CustomUseDetails me) {
        if (me == null) return false;

        return followService.isBlocked(me.getUser().getId(), user.getId());
    }

    @SchemaMapping(typeName = "UserProfileDTO", field = "user")
    public UserInfoDTO getUserInfo(UserProfileDTO userProfileDTO) {
        Long userId = userProfileDTO.getId();

        User user = userService.getUser(userId);

        return new UserInfoDTO(
                user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName(),
                user.getUserInfo().getAvatarUrl() == null ? "/icon/default-avatar.png" : user.getUserInfo().getAvatarUrl(),
                user.getUserInfo().getBirthday());
    }

    @SchemaMapping(typeName = "UserProfileDTO", field = "moments")
    public MomentConnection getMoments(
            UserProfileDTO profile,
            @Argument int page,
            @Argument int size) {
        Long userId = profile.getId();

        List<Moment> moments = momentService.findMomentsByUserId(userId, page, size);

        return new MomentConnection(
                moments.stream()
                        .map(ConvertHandler::convertMomentUserDTO)
                        .toList(),
                page,
                size
        );
    }

    @BatchMapping(typeName = "MomentUserDTO", field = "imgUrls")
    public Map<MomentUserDTO, List<String>> images(List<MomentUserDTO> moments) {
        List<Long> momentIds = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        System.out.println("CHECK BATCH Moment.img.url: " + momentIds);
        List<Image> images = imageService.getImages(momentIds);

        Map<Long, List<String>> map = images.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getMoment().getId(),
                        Collectors.mapping(ConvertHandler::convertImgUrl, Collectors.toList())

                ));

        return moments.stream()
                .collect(Collectors.toMap(
                        m -> m,
                        m -> map.getOrDefault(m.getId(), List.of())
                ));
    }

    @BatchMapping(typeName = "MomentUserDTO", field = "comments")
    public Map<MomentUserDTO, List<CommentDTO>> getComments(List<MomentUserDTO> moments) {
        List<Long> momentIds = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        System.out.println("CHECK BATCH Moment.comment: " + momentIds);

        List<Comment> comments = commentService.getCommentsByIds(momentIds);

        Map<Long, List<CommentDTO>> map = comments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getMoment().getId(),
                        Collectors.mapping(ConvertHandler::convertToCommentDTO, Collectors.toList())
                ));

        return moments.stream()
                .collect(Collectors.toMap(
                        m -> m,
                        m -> map.getOrDefault(m.getId(), List.of())
                ));
    }

    @BatchMapping(typeName = "MomentUserDTO", field = "hearts")
    public Map<MomentUserDTO, List<HeartDTO>> getHearts(List<MomentUserDTO> moments) {
        List<Long> momentId = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        System.out.println("CHECK BATCH Moment.heart: " + momentId);

        List<Heart> hearts = heartService.getAllByMomentIds(momentId);

        Map<Long, List<HeartDTO>> map = hearts.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getMoment().getId(),
                        Collectors.mapping(ConvertHandler::convertToHeartDTO, Collectors.toList())
                ));

        return moments.stream()
                .collect(Collectors.toMap(
                        m -> m,
                        m -> map.getOrDefault(m.getId(), List.of())
                ));
    }
}

/* @BatchMapping(typeName = "UserProfileDTO", field = "moments")
    public Map<UserProfileDTO, List<MomentUserDTO>> getMomentUsers(List<UserProfileDTO> profiles) {

        List<Long> userIds = profiles.stream()
                .map(UserProfileDTO::getId)
                .distinct()
                .toList();
        System.out.println("CHECK BATCH Moment.user: " + userIds);

        List<Moment> moments = momentService.findAllByUserIds(userIds);

        Map<Long, List<MomentUserDTO>> momentMap = moments.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getUser().getId(), //Group theo userId
                        Collectors.mapping(ConvertHandler::convertMomentUserDTO, Collectors.toList())
                ));

        return profiles.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> momentMap.getOrDefault(p.getId(), List.of())
                ));
    }
 */
