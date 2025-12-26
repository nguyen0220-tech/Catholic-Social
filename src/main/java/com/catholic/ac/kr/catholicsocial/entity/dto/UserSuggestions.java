package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UserSuggestions {
    private Long id;
    private String firstName;
    private String lastName;
    private String userAvatarUrl;
    private String fistNameUserSuggestions;
    private String lastNameUserSuggestions;
}
