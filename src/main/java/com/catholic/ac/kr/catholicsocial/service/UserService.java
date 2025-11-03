package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindUsernameRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UserRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Role;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.entity.model.UserInfo;
import com.catholic.ac.kr.catholicsocial.mapper.UserMapper;
import com.catholic.ac.kr.catholicsocial.repository.RoleRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.repository.VerificationTokenRepository;
import com.catholic.ac.kr.catholicsocial.security.tokencommon.VerificationTokenService;
import com.catholic.ac.kr.catholicsocial.status.Sex;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;

    public ApiResponse<Page<UserDTO>> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").descending());

        Page<User> users = userRepository.findAll(pageable);

        Page<UserDTO> userDTOS = users.map(UserMapper::toUserDTO);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "All users", userDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> createdUser(UserRequest request) {
        User newUser = new User();

        Role role = EntityUtils.getOrThrow(roleRepository.findByName("ROLE_USER"), "Role");

        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode("123"));
        newUser.setEnabled(true);
        newUser.setRoles(Set.of(role));

        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setFirstName(request.getFirstName());
        newUserInfo.setLastName(request.getLastName());
        newUserInfo.setEmail(request.getEmail());
        newUserInfo.setPhone(request.getPhoneNumber());
        newUserInfo.setSex(Sex.UNKNOWN);
        newUserInfo.setBirthday(request.getBirthday());

        newUser.setInfo(newUserInfo);

        userRepository.save(newUser);

        return ApiResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                "User created successfully", UserMapper.toUserDTO(newUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ApiResponse<String> deleteUser(Long userId) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        userRepository.delete(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "User deleted successfully");
    }

    public ApiResponse<String> findUsernameForgot(FindUsernameRequest request) {
        User user = EntityUtils.getOrThrow(userRepository.findByUserInfo_FirstNameAndLastNameAndPhone(
                        request.getFirstName(), request.getLastName(), request.getPhone()), "User");

        boolean isSendToken = verificationTokenRepository.existsByUser(user);

        if (isSendToken) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Đã gửi email , vui lòng kiểm tra lại email");
        }

        verificationTokenService.sendUsernameForgot(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Find username successfully, verify your email");
    }
//    public ApiResponse<String> forgotPassword(String email) {}
}

//reset mat khau can xac minh them so thich...thong tin user
