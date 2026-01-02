package com.catholic.ac.kr.catholicsocial.resolver;

import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDetailDTO;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MomentResolver {
    private  final MomentService momentService;

    public MomentDetailDTO momentDetail(@Argument Long momentId) {
        return momentService.getMoment(momentId);
    }
}
