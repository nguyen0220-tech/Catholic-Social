package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.HeartDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentUserDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.*;

public class ConvertHandler {

    public static UserGQLDTO convertToUserGQLDTO(User user) {
        UserGQLDTO userGQLDTO = new UserGQLDTO();

        userGQLDTO.setId(user.getId());
        userGQLDTO.setUserFullName(user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName());
        userGQLDTO.setAvatarUrl(user.getUserInfo().getAvatarUrl());

        return userGQLDTO;
    }

    public static MomentUserDTO convertMomentUserDTO(Moment moment) {
        MomentUserDTO momentUserDTO = new MomentUserDTO();

        momentUserDTO.setId(moment.getId());
        momentUserDTO.setContent(moment.getContent());
        momentUserDTO.setCreatedAt(moment.getCreatedAt());
        momentUserDTO.setShare(String.valueOf(moment.getShare()));

        return momentUserDTO;
    }

    public static String convertImgUrl(Image image) {
        return image.getImageUrl();
    }

    public static CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(comment.getId());
        commentDTO.setComment(comment.getComment());
        commentDTO.setCommentDate(comment.getCreatedAt());
        commentDTO.setUserId(comment.getUser().getId());

        return commentDTO;
    }

    public static HeartDTO convertToHeartDTO(Heart heart) {
        HeartDTO heartDTO = new HeartDTO();

        heartDTO.setHeartId(heart.getId());
        heartDTO.setUserId(heart.getUser().getId());

        return heartDTO;
    }
}

/*
khi mapper thì chỉ map những trường scale (nguyên thủy) còn những object thì để BatchMapping
 */
