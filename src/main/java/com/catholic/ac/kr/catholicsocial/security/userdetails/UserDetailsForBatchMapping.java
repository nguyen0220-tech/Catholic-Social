package com.catholic.ac.kr.catholicsocial.security.userdetails;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class UserDetailsForBatchMapping {

    public CustomUseDetails getCustomUseDetails(Principal principal) {

        CustomUseDetails customUseDetails;

        if (principal instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof CustomUseDetails) {
            customUseDetails = (CustomUseDetails) token.getPrincipal();

            return customUseDetails;
        }
        return null;
    }
}
