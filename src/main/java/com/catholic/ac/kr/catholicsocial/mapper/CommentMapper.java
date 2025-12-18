package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.projection.CommentProjection;

import java.util.List;

public class CommentMapper {
    public static CommentDTO toDTO(CommentProjection projection) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(projection.getId());
        commentDTO.setComment(projection.getComment());
        commentDTO.setCommentDate(projection.getCreatedAt());

        commentDTO.setMomentId(projection.getMomentId());
        commentDTO.setUserId(projection.getUserId());

        return commentDTO;
    }

    public static List<CommentDTO> toDTOList(List<CommentProjection> projections) {
        return projections.stream()
                .map(CommentMapper::toDTO)
                .toList();

    }
}
