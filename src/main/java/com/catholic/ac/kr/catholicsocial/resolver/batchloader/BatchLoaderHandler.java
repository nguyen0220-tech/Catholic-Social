package com.catholic.ac.kr.catholicsocial.resolver.batchloader;

import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.UserDetailsForBatchMapping;
import com.catholic.ac.kr.catholicsocial.service.FollowService;
import com.catholic.ac.kr.catholicsocial.service.UserService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BatchLoaderHandler {
    private final UserService userService;
    private final UserDetailsForBatchMapping userDetailsForBatchMapping;
    private final FollowService followService;

    public BatchLoaderHandler(UserService userService, UserDetailsForBatchMapping userDetailsForBatchMapping, FollowService followService) {
        this.userService = userService;
        this.userDetailsForBatchMapping = userDetailsForBatchMapping;
        this.followService = followService;
    }

    public <T> Map<T, UserGQLDTO> batchLoadUser(List<T> source, Function<T, Long> userIdExtractor) {
        List<Long> userIds = source.stream()
                .map(userIdExtractor)
                .toList();

        List<User> users = userService.getAllById(userIds);

        Map<Long, UserGQLDTO> usersMap = users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        ConvertHandler::convertToUserGQLDTO
                ));

        return source.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> usersMap.get(userIdExtractor.apply(s))
                ));
    }
    /*
    Function<T, Long> f:
        input:T
        output: Long
     */

    public <T> Map<T, Boolean> batchLoadFollow(List<T> source, Function<T, Long> userIdExtractor, Principal principal) {
        CustomUserDetails userDetails = userDetailsForBatchMapping.getCustomUserDetails(principal);

        Long myId = userDetails.getUser().getId();

        List<Long> userIds = source.stream()
                .map(userIdExtractor)
                .toList();

        List<Long> userIdsFollowing = followService.getUserIdsFollowing(myId, userIds);

        Set<Long> setUserIds = new HashSet<>(userIdsFollowing);

        return source.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> setUserIds.contains(userIdExtractor.apply(s))
                ));
    }
}
