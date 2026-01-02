package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MomentDetailDTO {
    private Long id;
    private Long actorId; //chủ moment
    private boolean isHeart; //đã heart hay chưa
    private boolean isFollowingActor; //đã theo dõi actor hay chưa
    private boolean mySelf;
    private UserGQLDTO actor;
    private MomentUserDTO moment;
}
