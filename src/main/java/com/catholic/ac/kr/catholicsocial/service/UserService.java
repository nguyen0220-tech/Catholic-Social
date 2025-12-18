package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.ProfileDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserFollowDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindPasswordRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindUsernameRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.ResetPasswordRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UserRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Role;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.entity.model.UserInfo;
import com.catholic.ac.kr.catholicsocial.entity.model.VerificationToken;
import com.catholic.ac.kr.catholicsocial.mapper.UserMapper;
import com.catholic.ac.kr.catholicsocial.repository.RoleRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.repository.VerificationTokenRepository;
import com.catholic.ac.kr.catholicsocial.security.tokencommon.VerificationTokenService;
import com.catholic.ac.kr.catholicsocial.status.Sex;
import com.catholic.ac.kr.catholicsocial.uploadfile.UploadFileHandler;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UploadFileHandler uploadFileHandler;

    public List<User> findAllById(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

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

        boolean isSendToken = checkSendEmail(user);

        if (isSendToken) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Đã gửi email , vui lòng kiểm tra lại email");
        }

        verificationTokenService.sendUsernameForgot(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Find username successfully, verify your email");
    }

    public ApiResponse<String> forgotPassword(FindPasswordRequest request) {
        User user = EntityUtils.getOrThrow(userRepository.
                findByUserInfo_UsernameAndEmailAndPhone(
                        request.getUsername(),
                        request.getEmail(),
                        request.getPhone()), "User ");

        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByUser(user);

        verificationToken.ifPresent(t -> {
            if (t.getExpiryDate().isBefore(LocalDateTime.now())) {
                verificationTokenRepository.delete(t);
            }
        });

        boolean isSendToken = checkSendEmail(user);

        if (isSendToken) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Đã gửi email , vui lòng kiểm tra lại email");
        }


        verificationTokenService.sendEmailFindPassword(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Find username successfully, verify your email");
    }

    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        VerificationToken token = EntityUtils.getOrThrow(
                verificationTokenRepository.findByToken(request.getToken()), "Token");

        if (token == null || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Token already expired/Token không hợp lệ hoặc đã hết hạn");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Mật khẩu nhập lại không trùng");
        }

        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        verificationTokenRepository.delete(token);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Thay đổi mật khẩu thành công");

    }

    private boolean checkSendEmail(User user) {
        return verificationTokenRepository.existsByUser(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<ProfileDTO> getUserProfile(Long userId) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        ProfileDTO profileDTO = UserMapper.toProfileDTO(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Get user profile success", profileDTO);

    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> updateProfile(Long userId, ProfileDTO request) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        boolean existsPhone = userRepository.existsUserByPhone(request.getPhone());

        //chua hoat dong dung
        if (existsPhone && !user.getUserInfo().getPhone().equals(request.getPhone())) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Phone already exists");
        }

        boolean existsEmail = userRepository.existsUserByEmail(request.getEmail());
        if (existsEmail && !user.getUserInfo().getEmail().equals(request.getEmail())) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    "Email already exists");
        }

        user.getUserInfo().setFirstName(request.getFirstName());
        user.getUserInfo().setLastName(request.getLastName());
        user.getUserInfo().setPhone(request.getPhone() != null ? request.getPhone() : user.getUserInfo().getPhone());
        user.getUserInfo().setEmail(request.getEmail() != null ? request.getEmail() : user.getUserInfo().getEmail());
        user.getUserInfo().setBirthday(request.getBirthDate());
        user.getUserInfo().setSex(Sex.valueOf(request.getGender()));
        user.getUserInfo().setAddress(request.getAddress());

        userRepository.save(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Update profile success");
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> uploadAvatar(Long userId, MultipartFile file) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        String avatarUrl = uploadFileHandler.uploadFile(user.getId(), file);

        user.getUserInfo().setAvatarUrl(avatarUrl);

        userRepository.save(user);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Update avatar success", avatarUrl);
    }

    public ApiResponse<List<UserFollowDTO>> findUserFollow(Long userId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<UserFollowDTO> userFollowDTO = userRepository.findUserFollowByUserId(userId, keyword, pageable);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Find user follow success", userFollowDTO);
    }
}