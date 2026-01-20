package com.catholic.ac.kr.catholicsocial.uploadfile;

import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class UploadFileHandler {
    public final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public UploadFileHandler(UserRepository userRepository, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    public String uploadFile(Long userId, MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap(
                            "folder", "media_" + userId,
                            "public_id", "user_id" + "_" + LocalDateTime.now(), //when delete file
                            "overwrite", true));

            return result.get("url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Upload fail: ", e);
        }
    }

    public Map<?, ?> uploadVideo(Long userId, MultipartFile file) {
        try {

            return (Map<?, ?>) cloudinary.uploader().uploadLarge(
                    file.getInputStream(),
                    ObjectUtils.asMap(
                            "folder", "media_" + userId,
                            "resource_type", "video"
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Upload fail: " + e.getMessage(), e);
        }
    }

}
