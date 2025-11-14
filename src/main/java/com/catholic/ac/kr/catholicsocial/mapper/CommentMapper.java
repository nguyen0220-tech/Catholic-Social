package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.convert.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentMapper {
    public static CommentDTO commentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(comment.getId());
        commentDTO.setComment(comment.getComment());
        commentDTO.setCommentDate(comment.getCreatedAt());

        UserGQLDTO userGQLDTO = ConvertHandler.convertToUserGQLDTO(comment.getUser());

        commentDTO.setUser(userGQLDTO);

        return commentDTO;
    }

    public static List<CommentDTO> commentDTOList(List<Comment> comments) {
        List<CommentDTO> commentDTOList = new ArrayList<>();

        for (Comment comment : comments) {
            commentDTOList.add(commentDTO(comment));
        }

        return commentDTOList;
    }
}
