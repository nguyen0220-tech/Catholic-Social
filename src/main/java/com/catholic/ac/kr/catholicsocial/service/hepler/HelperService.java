package com.catholic.ac.kr.catholicsocial.service.hepler;

import com.catholic.ac.kr.catholicsocial.repository.ChatRoomMemberRepository;
import com.catholic.ac.kr.catholicsocial.repository.FollowRepository;
import com.catholic.ac.kr.catholicsocial.service.FollowService;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class HelperService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final FollowRepository followRepository;
    private final FollowService followService;

    public void validateMember(Long currentUserId, Long chatRoomId) {
        boolean existing = chatRoomMemberRepository.existsByUser_IdAndChatRoom_IdAndStatus(
                currentUserId,
                chatRoomId,
                ChatRoomMemberStatus.ACTIVE
        );
        if (!existing) {
            throw new AccessDeniedException("forbidden");
        }
    }

    public void validateDirectMessage(Long senderId, Long recipientId) {
        if (senderId.equals(recipientId))
            throw new IllegalStateException("Cannot send to self");

        boolean blocked = followRepository.checkBlockTwoWay(
                senderId, recipientId, FollowState.BLOCKED
        );

        if (blocked)
            throw new IllegalStateException("Recipient blocked");
    }

    public List<Long> filterBlocked(Long userId, List<Long> memberIds) {
        Set<Long> blocked = new HashSet<>(followService.getUserIdsBlocked(userId));

        return memberIds.stream()
                .filter(id -> !blocked.contains(id))
                .toList();
    }
}
