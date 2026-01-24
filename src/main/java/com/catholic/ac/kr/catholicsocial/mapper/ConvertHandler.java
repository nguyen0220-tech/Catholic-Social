package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.model.*;

import java.util.List;

public class ConvertHandler {

    public static UserGQLDTO convertToUserGQLDTO(User user) {
        UserGQLDTO userGQLDTO = new UserGQLDTO();

        userGQLDTO.setId(user.getId());
        userGQLDTO.setUserFullName(user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName());
        userGQLDTO.setAvatarUrl(user.getUserInfo().getAvatarUrl() != null ?
                user.getUserInfo().getAvatarUrl() : "/icon/default-avatar.png");

        return userGQLDTO;
    }

    public static NotificationFollowerDTO convertToNotificationFollowerDTO(User user) {
        NotificationFollowerDTO notificationFollowerDTO = new NotificationFollowerDTO();

        notificationFollowerDTO.setFollowerId(user.getId());

        return notificationFollowerDTO;
    }

    public static NotificationCommentDTO convertToNotificationCommentDTO(Comment comment) {
        NotificationCommentDTO notificationCommentDTO = new NotificationCommentDTO();

        notificationCommentDTO.setMomentComment(comment.getComment());
        notificationCommentDTO.setMomentId(comment.getMoment().getId());

        return notificationCommentDTO;
    }

    public static MomentUserDTO convertMomentUserDTO(Moment moment) {
        MomentUserDTO momentUserDTO = new MomentUserDTO();

        momentUserDTO.setId(moment.getId());
        momentUserDTO.setContent(moment.getContent());
        momentUserDTO.setCreatedAt(moment.getCreatedAt());
        momentUserDTO.setShare(String.valueOf(moment.getShare()));

        return momentUserDTO;
    }

    public static MomentGQLDTO convertMomentGraphql(Moment moment) {
        MomentGQLDTO momentGQLDTO = new MomentGQLDTO();

        momentGQLDTO.setId(moment.getId());
        momentGQLDTO.setContent(moment.getContent());

        List<String> imgList = moment.getImages().stream()
                .map(Image::getImageUrl)
                .toList();

        momentGQLDTO.setImages(imgList);

        return momentGQLDTO;
    }

    public static String convertImgUrl(Image image) {
        return image.getImageUrl();
    }

    public static String convertMediaUrl(MessageMedia media) {
        return media.getUrl();
    }

    public static ChatRoomDetailDTO convertToChatRoomDetailDTO(ChatRoom chatRoom) {
        ChatRoomDetailDTO chatRoomDetailDTO = new ChatRoomDetailDTO();

        chatRoomDetailDTO.setRoomName(chatRoom.getRoomName());
        chatRoomDetailDTO.setDescription(chatRoom.getDescription());
        chatRoomDetailDTO.setLastMessagePreview(chatRoom.getLastMessagePreview());
        chatRoomDetailDTO.setLastMessageAt(chatRoom.getLastMessageAt());

        return chatRoomDetailDTO;
    }

    public static CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(comment.getId());
        commentDTO.setComment(comment.getComment());
        commentDTO.setCommentDate(comment.getCreatedAt());
        commentDTO.setMomentId(comment.getMoment().getId());
        commentDTO.setUserId(comment.getUser().getId());

        return commentDTO;
    }

    public static HeartDTO convertToHeartDTO(Heart heart) {
        HeartDTO heartDTO = new HeartDTO();

        heartDTO.setId(heart.getId());
        heartDTO.setUserId(heart.getUser().getId());
        heartDTO.setMomentId(heart.getMoment().getId());

        return heartDTO;
    }

    public static IntroContentDTO convertToIntroContentDTO(IntroVideo introVideo) {
        IntroContentDTO introContentDTO = new IntroContentDTO();

        introContentDTO.setUrl(introVideo.getUrl());
        introContentDTO.setExp(introVideo.getExp());
        introContentDTO.setPublicId(introVideo.getPublicId());

        return introContentDTO;
    }
}

/*
khi mapper thì chỉ map những trường scale (nguyên thủy) còn những object thì để BatchMapping
 */
