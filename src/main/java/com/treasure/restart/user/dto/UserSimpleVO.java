package com.treasure.restart.user.dto;

public record UserSimpleVO(
        Long id,
        String username,
        String nickname,
        String avatar
) {
}
