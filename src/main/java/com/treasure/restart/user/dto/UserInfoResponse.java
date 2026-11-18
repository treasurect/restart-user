package com.treasure.restart.user.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String coverUrl;
}
