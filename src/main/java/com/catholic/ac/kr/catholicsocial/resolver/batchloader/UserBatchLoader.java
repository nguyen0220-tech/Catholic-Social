package com.catholic.ac.kr.catholicsocial.resolver.batchloader;

import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.ConvertHandler;
import com.catholic.ac.kr.catholicsocial.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserBatchLoader {
    private final UserService userService;

    public UserBatchLoader(UserService userService) {
        this.userService = userService;
    }

    public Map<Long, UserGQLDTO> loadUserByIds(List<Long> userIds) {
        List<User> users = userService.findAllById(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        ConvertHandler::convertToUserGQLDTO
                ));
    }
}
