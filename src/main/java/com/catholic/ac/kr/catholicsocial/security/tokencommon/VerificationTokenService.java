package com.catholic.ac.kr.catholicsocial.security.tokencommon;

import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.entity.model.VerificationToken;
import com.catholic.ac.kr.catholicsocial.repository.VerificationTokenRepository;
import com.catholic.ac.kr.catholicsocial.security.systemservice.SendEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerificationTokenService {
    @Value("${app.verification.base-url}")
    private String baseUrl;

    private final VerificationTokenRepository verificationTokenRepository;
    private final SendEmailService sendEmailService;

    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();

        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    public void sendVerificationToken(User user) {
        String token = createVerificationToken(user);

        String verifyLink = baseUrl + "/auth/verify?token=" + token;

        sendEmailService.sendEmail(
                user.getUserInfo().getEmail(),
                "Please click link verify email",
                """
                        <div style="font-family:Arial, sans-serif; color:#333; padding:20px;">
                        <h2 style="color:#2d89ef;">Xin chào!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Catholic Social</strong>.</p>
                        <p>Vui lòng nhấn vào nút bên dưới để xác minh email của bạn:</p>
                        <a href="%s"\s
                        style="display:inline-block; background-color:#2d89ef; color:#fff;\s
                        padding:10px 20px; border-radius:5px; text-decoration:none; font-weight:bold;">
                        Xác minh email
                        </a>
                        <p style="margin-top:20px; font-size:12px; color:#777;">
                        Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này
                        </p>
                        </div>
                        """.formatted(verifyLink)
        );
    }

    public void sendUsernameForgot(User user) {
        String token = createVerificationToken(user);

        String verifyLink = baseUrl + "/auth/verify?token=" + token;

        sendEmailService.sendEmail(
                user.getUserInfo().getEmail(),
                "Thông tin tài khoản của bạn tại Catholic Social",
                """
                        <div style="font-family:Arial, sans-serif; color:#333; padding:20px; background:#f7f7f7;">
                            <div style="max-width:600px; margin:0 auto; background:#fff; padding:20px; border-radius:10px; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                                <h2 style="color:#2d89ef;">Xin chào %s %s!</h2>
                                <p>Bạn đã yêu cầu lấy lại thông tin tài khoản của mình tại <strong>Catholic Social</strong>.</p>
                                <p><strong>Tài khoản đăng nhập của bạn:</strong> %s</p>
                                <p>Để bảo mật, vui lòng nhấn vào nút bên dưới để xác nhận rằng đây là bạn:</p>
                                <a href="%s"
                                   style="display:inline-block; background-color:#2d89ef; color:#fff;
                                          padding:10px 20px; border-radius:5px; text-decoration:none; font-weight:bold;">
                                   Xác nhận tài khoản
                                </a>
                                <p style="margin-top:20px; font-size:12px; color:#777;">
                                    Nếu bạn không yêu cầu thông tin này, vui lòng bỏ qua email này.
                                </p>
                            </div>
                        </div>
                        """.formatted(
                        user.getUserInfo().getFirstName(),
                        user.getUserInfo().getLastName(),
                        user.getUsername(),
                        verifyLink
                )
        );
    }

    public void sendEmailFindPassword(User user) {
        String token = createVerificationToken(user);

        String verifyLink = baseUrl + "/user/verify-reset-password?token=" + token;

        sendEmailService.sendEmail(
                user.getUserInfo().getEmail(),
                "Thông tin tài khoản của bạn tại Catholic Social",
                """
                        <div style="font-family:Arial, sans-serif; color:#333; padding:20px; background:#f7f7f7;">
                            <div style="max-width:600px; margin:0 auto; background:#fff; padding:20px; border-radius:10px; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                                <h2 style="color:#2d89ef;">Xin chào %s %s!</h2>
                                <p>Bạn đã yêu cầu tìm mật khẩu của mình tại <strong>Catholic Social</strong>.</p>
                                <p>Để bảo mật, vui lòng nhấn vào nút bên dưới để xác nhận rằng đây là bạn:</p>
                                <a href="%s"
                                   style="display:inline-block; background-color:#2d89ef; color:#fff;
                                          padding:10px 20px; border-radius:5px; text-decoration:none; font-weight:bold;">
                                   Xác nhận tài khoản
                                </a>
                                <p style="margin-top:20px; font-size:12px; color:#777;">
                                    Nếu bạn không yêu cầu thông tin này, vui lòng bỏ qua email này.
                                </p>
                            </div>
                        </div>
                        """.formatted(
                        user.getUserInfo().getFirstName(),
                        user.getUserInfo().getLastName(),
                        verifyLink
                )
        );
    }
}
