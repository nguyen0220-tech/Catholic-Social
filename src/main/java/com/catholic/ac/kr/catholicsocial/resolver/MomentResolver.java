package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDetailDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentUserDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.FollowService;
import com.catholic.ac.kr.catholicsocial.service.HeartService;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import com.catholic.ac.kr.catholicsocial.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MomentResolver {
    private final MomentService momentService;
    private final UserService userService;
    private final HeartService heartService;
    private final FollowService followService;

    @QueryMapping
    public MomentDetailDTO momentDetail(@Argument Long momentId) {
        return momentService.getMomentDetail(momentId);
    }

    @SchemaMapping(typeName = "MomentDetail", field = "moment")
    public MomentUserDTO moment(MomentDetailDTO detail) {
        Long momentId = detail.getId();

        Moment moment = momentService.getMoment(momentId);

        return ConvertHandler.convertMomentUserDTO(moment);
    }

    @SchemaMapping(typeName = "MomentDetail", field = "actor")
    public UserGQLDTO user(MomentDetailDTO detail) {
        Long userId = detail.getActorId();

        User user = userService.getUser(userId);

        return ConvertHandler.convertToUserGQLDTO(user);
    }

    @SchemaMapping(typeName = "MomentDetail", field = "isHeart")
    public boolean isHeart(
            @AuthenticationPrincipal CustomUserDetails me,
            MomentDetailDTO detail) {

        Long momentId = detail.getId();

        return heartService.checkHeart(me.getUser().getId(), momentId);
    }

    @SchemaMapping(typeName = "MomentDetail", field = "isFollowing")
    public boolean isFollowing(
            @AuthenticationPrincipal CustomUserDetails me,
            MomentDetailDTO detail) {

        Long actorId = detail.getActorId();

        return followService.isFollowing(me.getUser().getId(), actorId);
    }

    @SchemaMapping(typeName = "MomentDetail",field = "mySelf")
    public boolean mySelf(
            @AuthenticationPrincipal CustomUserDetails me,
            MomentDetailDTO detail) {

        Long meId = me.getUser().getId();
        Long actorId = detail.getActorId();

        return meId.equals(actorId);
    }
}
