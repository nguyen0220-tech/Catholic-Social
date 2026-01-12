package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.CommentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.CommentRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Active;
import com.catholic.ac.kr.catholicsocial.entity.model.Comment;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.CommentMapper;
import com.catholic.ac.kr.catholicsocial.projection.CommentProjection;
import com.catholic.ac.kr.catholicsocial.repository.*;
import com.catholic.ac.kr.catholicsocial.status.ActiveType;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import com.catholic.ac.kr.catholicsocial.status.MomentShare;
import com.catholic.ac.kr.catholicsocial.status.NotifyType;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final FollowRepository followRepository;
    private final ActiveRepository activeRepository;
    private final NotificationService notificationService;

    public List<Comment> getCommentsByMomentIds(List<Long> momentIds) {
        return commentRepository.findAllByMoment_IdIn(momentIds);
    }

    public List<Comment> getCommentsByIds(List<Long> ids) {
        return commentRepository.findAllById(ids);
    }

    public ListResponse<CommentDTO> getCommentsByMomentId(Long currentUserId, Long momentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<CommentProjection> commentProjections = commentRepository.findByMomentId(momentId, pageable);

        //filter comments of users in list un-block
        Set<Long> setBlocked = new HashSet<>(followRepository.findUserIdsBlocked(currentUserId));

        List<CommentProjection> filterComments = commentProjections.stream()
                .filter(c -> !setBlocked.contains(c.getUserId()))
                .toList();

        List<CommentDTO> dos = CommentMapper.toDTOList(filterComments);
        return new ListResponse<>(dos);
    }

    public GraphqlResponse<String> createComment(Long userId, Long momentId, CommentRequest request) {

        User currentUser = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        Moment moment = EntityUtils.getOrThrow(momentRepository.findById(momentId), "Moment");

        boolean followerBlock = followRepository.existsByFollowerAndUserAndState(currentUser, moment.getUser(), FollowState.BLOCKED);
        boolean ownerMomentBlock = followRepository.existsByFollowerAndUserAndState(moment.getUser(), currentUser, FollowState.BLOCKED);

        if (followerBlock || ownerMomentBlock) {
            throw new GraphQLException("You cannot comment.Because you blocked");
        }

        //  Nếu PRIVATE → chỉ chủ sở hữu được comment
        if (moment.getShare() == MomentShare.PRIVATE &&
                !moment.getUser().getId().equals(currentUser.getId())) {
            throw new GraphQLException("You cannot comment on a private moment");
        }

        // Nếu FOLLOWER → chỉ follower hoặc chủ sở hữu được comment
        if (moment.getShare() == MomentShare.FOLLOWER &&
                !moment.getUser().getId().equals(currentUser.getId())) {

            boolean isFollowed = followRepository.existsByFollowerAndUserAndState(
                    currentUser, moment.getUser(), FollowState.FOLLOWING
            );

            if (!isFollowed) {
                throw new GraphQLException("Only followers can comment on this moment");
            }
        }

        //  Nếu PUBLIC hoặc đã pass check → cho phép comment
        Comment comment = new Comment();
        comment.setUser(currentUser);
        comment.setMoment(moment);
        comment.setComment(request.getComment());

        commentRepository.save(comment);

        Active newActive = Active.builder()
                .user(currentUser)
                .entityId(comment.getId())
                .type(ActiveType.COMMENT_MOMENT)
                .build();

        activeRepository.save(newActive);

        if (!(userId.equals(moment.getUser().getId())))
            notificationService.createNotification(moment.getUser(), currentUser, comment.getId(), NotifyType.COMMENT_MOMENT);

        return GraphqlResponse.success("comment success", null);
    }

}
