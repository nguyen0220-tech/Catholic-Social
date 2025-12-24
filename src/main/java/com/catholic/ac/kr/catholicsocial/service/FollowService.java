package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.FollowDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Follow;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.FollowMapper;
import com.catholic.ac.kr.catholicsocial.repository.FollowRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.ACTION;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public boolean isFollowing(Long currentUserId, Long userId) {
        User currentUser = EntityUtils.getOrThrow(userRepository.findById(currentUserId),"User");
        User user = EntityUtils.getOrThrow(userRepository.findById(userId),"User");

        return followRepository.existsByFollowerAndUserAndState(currentUser, user, FollowState.FOLLOWING);
    }

    public boolean isBlocked(Long currentUserId, Long userId) {
        User currentUser = EntityUtils.getOrThrow(userRepository.findById(currentUserId),"User");
        User user = EntityUtils.getOrThrow(userRepository.findById(userId),"User");

        return followRepository.existsByFollowerAndUserAndState(currentUser, user, FollowState.BLOCKED);
    }

    //Danh sách mình đang theo dõi người khác
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<FollowDTO> getAllFollowers(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());

        List<Follow> follows = followRepository.findByFollowerIdAndState(currentUserId, FollowState.FOLLOWING, pageable);
        FollowDTO followDTOS = FollowMapper.mapFollowingUses(follows);

        int followingUserCount = followRepository.countFollowByFollowerIdAndState(currentUserId, FollowState.FOLLOWING);
        followDTOS.setUserNums(followingUserCount);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Danh sách đang theo dõi", followDTOS);
    }

    //Danh sách người đang theo dõi mình
    public ApiResponse<FollowDTO> getAllUsersFollowing(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());

        List<Follow> follows = followRepository.findByUserIdAndState(currentUserId, FollowState.FOLLOWING, pageable);
        FollowDTO followDTOS = FollowMapper.mapUsersFollowing(follows);

        int userFollowingCount = followRepository.countFollowByUserIdAndState(currentUserId, FollowState.FOLLOWING);
        followDTOS.setUserNums(userFollowingCount);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Danh sách người theo dõi", followDTOS);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<FollowDTO> getBlockedFollowers(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());

        List<Follow> listBlockedFollowers = followRepository.findByFollowerIdAndState(currentUserId, FollowState.BLOCKED, pageable);
        FollowDTO blockedDTOS = FollowMapper.mapFollowingUses(listBlockedFollowers);

        int blockedUserCount = followRepository.countFollowByFollowerIdAndState(currentUserId, FollowState.BLOCKED);
        blockedDTOS.setUserNums(blockedUserCount);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Get all blocked followers", blockedDTOS);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Transactional
    public ApiResponse<String> createFollower(Long followerId, Long userId) {
        User follower = getUserById(followerId);

        User user = getUserById(userId);

        //check tự theo dõi chính minh: mặc dù khi tìm kiếm user sẽ không trả về kết quả gồm bản thân
        if (follower.getId().equals(user.getId())) {
            return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Không thể tự theo dõi chính minh");
        }

        //check có bị block OR đang block hay chưa? : mặc dù khi tìm kiếm user sẽ không hiện ra user trong danh sách blocked
        boolean isBlock = followRepository.existsByFollowerAndUserAndState(user, follower, FollowState.BLOCKED);
        boolean isBlocking = followRepository.existsByFollowerAndUserAndState(follower, user, FollowState.BLOCKED);

        if (isBlock || isBlocking) {
            return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Follower already blocked");
        }

        //check có followed hay chưa?
        boolean isFollow = followRepository.existsByFollowerAndUserAndState(follower, user, FollowState.FOLLOWING);
        if (isFollow) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Bạn đã theo dõi người dùng này.");
        }

        //nếu chưa sẽ tạo follow mới, nếu đã từng followed -> cancelled: follow lại
        Optional<Follow> followOpt = followRepository.findByFollowerAndUser(follower, user);

        if (followOpt.isEmpty()) {
            Follow newFollow = new Follow();

            newFollow.setFollower(follower);
            newFollow.setUser(user);
            newFollow.setState(FollowState.FOLLOWING);

            followRepository.save(newFollow);

            return ApiResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                    "Đã theo dõi thành công");
        }

        Follow follow = followOpt.get();

        follow.setState(FollowState.FOLLOWING);

        followRepository.save(follow);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Đã theo dõi thành công");

    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> userAction(Long followerId, Long userId, ACTION action) {
        User follower = getUserById(followerId);

        User user = getUserById(userId);

        Follow following = EntityUtils.getOrThrow(followRepository.findByFollowerAndUser_Action(follower, user), "Following");

        if (ACTION.UNFOLLOW.equals(action) || ACTION.UNBLOCK.equals(action)) {
            following.setState(FollowState.CANCELLED);
        }

        followRepository.save(following);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Đã " + action + " thành công");
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> blockUser(Long followerId, Long userId) {

        User currentUser = getUserById(followerId);
        User user = getUserById(userId);

        // 1. currentUser → user = BLOCKED (create or update)
        Follow block = followRepository
                .findByFollowerAndUser_Action(currentUser, user)
                .orElseGet(() -> {
                    Follow f = new Follow();
                    f.setFollower(currentUser);
                    f.setUser(user);
                    return f;
                });

        block.setState(FollowState.BLOCKED);
        followRepository.save(block);

        // 2. user → currentUser = CANCELLED (nếu tồn tại)
        followRepository.findByFollowerAndUser_Action(user, currentUser)
                .ifPresent(f -> {
                    f.setState(FollowState.CANCELLED);
                    followRepository.save(f);
                });

        return ApiResponse.success(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Đã block thành công!"
        );
    }

    private User getUserById(Long userId) {
        return EntityUtils.getOrThrow(userRepository.findById(userId), "User");
    }
}