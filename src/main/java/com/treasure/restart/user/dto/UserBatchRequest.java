package com.treasure.restart.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserBatchRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 100, message = "单次最多查询100个用户")
    private List<@NotNull(message = "用户ID不能为null") Long> ids;
}
