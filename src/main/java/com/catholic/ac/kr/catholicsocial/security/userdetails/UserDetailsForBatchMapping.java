package com.catholic.ac.kr.catholicsocial.security.userdetails;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class UserDetailsForBatchMapping {

    public CustomUserDetails getCustomUserDetails(Principal principal) {

        CustomUserDetails customUserDetails;

        if (principal instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof CustomUserDetails) {
            customUserDetails = (CustomUserDetails) token.getPrincipal();

            return customUserDetails;
        }
        return null;
    }
}
