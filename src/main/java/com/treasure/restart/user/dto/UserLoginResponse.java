package com.treasure.restart.user.dto;

import lombok.Data;

@Data
public class UserLoginResponse {

    private String token;

    private Long userId;

    private String username;

    private String nickname;
}