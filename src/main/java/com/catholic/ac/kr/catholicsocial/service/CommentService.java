package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.CommentRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Comment;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.CommentMapper;
import com.catholic.ac.kr.catholicsocial.repository.CommentRepository;
import com.catholic.ac.kr.catholicsocial.repository.FollowRepository;
import com.catholic.ac.kr.catholicsocial.repository.MomentRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import com.catholic.ac.kr.catholicsocial.status.MomentShare;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final FollowRepository followRepository;

    public ApiResponse<List<CommentDTO>> getCommentsByMomentId(Long momentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<Comment> comments = commentRepository.findByMomentId(momentId, pageable);

        List<CommentDTO> commentDTOS = CommentMapper.commentDTOList(comments);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Get all comments", commentDTOS);
    }

    public ApiResponse<String> createComment(Long userId, Long momentId, CommentRequest request) {

        User currentUser = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        Moment moment = EntityUtils.getOrThrow(momentRepository.findById(momentId), "Moment");

        boolean followerBlock = followRepository.existsByFollowerAndUserAndState(currentUser,moment.getUser(),FollowState.BLOCKED);
        boolean ownerMomentBlock = followRepository.existsByFollowerAndUserAndState(moment.getUser(),currentUser,FollowState.BLOCKED);

        if (followerBlock || ownerMomentBlock) {
            return ApiResponse.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "You cannot comment.Because you blocked");
        }

        //  Nếu PRIVATE → chỉ chủ sở hữu được comment
        if (moment.getShare() == MomentShare.PRIVATE &&
                !moment.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "You cannot comment on a private moment");
        }

        // Nếu FOLLOWER → chỉ follower hoặc chủ sở hữu được comment
        if (moment.getShare() == MomentShare.FOLLOWER &&
                !moment.getUser().getId().equals(currentUser.getId())) {

            boolean isFollowed = followRepository.existsByFollowerAndUserAndState(
                    currentUser, moment.getUser(), FollowState.FOLLOWING
            );

            if (!isFollowed) {
                return ApiResponse.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                        "Only followers can comment on this moment");
            }
        }

        //  Nếu PUBLIC hoặc đã pass check → cho phép comment
        Comment comment = new Comment();
        comment.setUser(currentUser);
        comment.setMoment(moment);
        comment.setComment(request.getComment());

        commentRepository.save(comment);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Created comment successfully");
    }

}
