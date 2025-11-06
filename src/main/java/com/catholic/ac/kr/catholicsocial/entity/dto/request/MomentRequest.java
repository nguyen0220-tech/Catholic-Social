package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import com.catholic.ac.kr.catholicsocial.status.MomentShare;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MomentRequest {
    private String content;
    private List<MultipartFile> files = new ArrayList<>();
    @NotNull(message = "Vui lòng chọn chế độ chia sẻ")
    private MomentShare share;
}
