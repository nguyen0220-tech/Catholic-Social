package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.model.*;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.*;
import com.catholic.ac.kr.catholicsocial.wrapper.MomentConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;
    private final ImageService imageService;
    private final CommentService commentService;
    private final MomentService momentService;
    private final HeartService heartService;
    private final FollowService followService;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;
    private final SavedService savedService;

    @QueryMapping
    public UserProfileDTO profile(@Argument Long userId) {
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
    public UserInfoDTO user(UserProfileDTO userProfileDTO) {
        Long userId = userProfileDTO.getId();

        User user = userService.getUser(userId);

        return new UserInfoDTO(
                user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName(),
                user.getUserInfo().getAvatarUrl() == null ? "/icon/default-avatar.png" : user.getUserInfo().getAvatarUrl(),
                user.getUserInfo().getBirthday());
    }

    @SchemaMapping(typeName = "UserProfileDTO", field = "moments")
    public MomentConnection moments(
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

        log.info("CHECK BATCH Moment.img.url: {}", momentIds);

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

    @BatchMapping(typeName = "MomentUserDTO", field = "saved")
    public Map<MomentUserDTO, Boolean> saved(List<MomentUserDTO> moments, Principal principal) {
        CustomUseDetails me = userDetailsForBatchMapping.getCustomUseDetails(principal);
        Long myId = me.getUser().getId();

        List<Long> momentIds = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        // Set momentId mà user đang đăng nhập đã saved
        Set<Long> savedMomentIdList = new HashSet<>(savedService.getAllByMomentIds(myId, momentIds));

        System.out.println("CHECK BATCH: " + savedMomentIdList);

        return moments.stream()
                .collect(Collectors.toMap(
                        m -> m,
                        m -> savedMomentIdList.contains(m.getId())
                ));
    }

    @BatchMapping(typeName = "MomentUserDTO", field = "comments")
    public Map<MomentUserDTO, List<CommentDTO>> comments(List<MomentUserDTO> moments, Principal principal) {
        /*.
        // lấy thông tin phiên đăng nhập hiện tại. Đây là cách chuẩn trong Spring Security để lấy user ở bất kỳ đâu
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUseDetails me = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUseDetails) {
            me = (CustomUseDetails) authentication.getPrincipal();
        }
         */

        CustomUseDetails me = userDetailsForBatchMapping.getCustomUseDetails(principal);

        List<Long> momentIds = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        log.info("CHECK BATCH Moment.comment: {}", momentIds);

        List<Comment> comments = commentService.getCommentsByMomentIds(momentIds);

        List<Long> myBlockedUserIds = me == null ?
                List.of() : followService.getUserIdsBlocked(me.getUser().getId()); //ds userId mà currentId đang đăng nhập chặn

        Set<Long> setBlocked = new HashSet<>(myBlockedUserIds);
        List<Comment> rs = comments.stream() //loại những comment của user bị chặn
                .filter(c -> !setBlocked.contains(c.getUser().getId()))
                .toList();

        if (me != null)
            System.out.println(("my id: " + me.getUser().getId()));
        System.out.println("myBlockedUserIds: " + myBlockedUserIds);
        System.out.println("userIds un-block" + rs.stream().map(Comment::getUser).map(User::getId).toList());

        Map<Long, List<CommentDTO>> map = rs.stream()
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
    public Map<MomentUserDTO, List<HeartDTO>> hearts(List<MomentUserDTO> moments, Principal principal) {
        CustomUseDetails me = userDetailsForBatchMapping.getCustomUseDetails(principal);

        List<Long> momentId = moments.stream()
                .map(MomentUserDTO::getId)
                .toList();

        log.info("CHECK BATCH Moment.heart: {}", momentId);

        List<Heart> hearts = heartService.getAllByMomentIds(momentId);

        List<Long> myBlockedUserIds = me == null ?
                List.of() : followService.getUserIdsBlocked(me.getUser().getId());

        Set<Long> setBlocked = new HashSet<>(myBlockedUserIds);
        List<Heart> rs = hearts.stream()
                .filter(h -> !setBlocked.contains(h.getUser().getId()))
                .toList();

        Map<Long, List<HeartDTO>> map = rs.stream()
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

/*.
 @BatchMapping(typeName = "UserProfileDTO", field = "moments")
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
