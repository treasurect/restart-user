package com.treasure.restart.user.controller;

import com.treasure.common.response.BaseResponse;
import com.treasure.restart.user.dto.UserInfoResponse;
import com.treasure.restart.user.dto.UserLoginRequest;
import com.treasure.restart.user.dto.UserLoginResponse;
import com.treasure.restart.user.dto.UserUpdateRequest;
import com.treasure.restart.user.dto.UserBatchRequest;
import com.treasure.restart.user.dto.UserSimpleVO;
import com.treasure.restart.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/loginPwd")
    public BaseResponse<UserLoginResponse> loginPwd(@Valid @RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse data = userService.loginPwd(request);
            return BaseResponse.success("登录成功",data);
        } catch (IllegalArgumentException e) {
            return BaseResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/info")
    public BaseResponse<UserInfoResponse> info() {
        UserInfoResponse userInfo = userService.getUserInfo();
        return BaseResponse.success("用户信息获取成功" , userInfo);
    }

    @GetMapping("/{id}")
    public BaseResponse<UserSimpleVO> getById(@PathVariable Long id) {
        return BaseResponse.success("用户信息获取成功", userService.getUserById(id));
    }

    @PostMapping("/batch")
    public BaseResponse<List<UserSimpleVO>> batch(@Valid @RequestBody UserBatchRequest request) {
        return BaseResponse.success("用户批量查询成功", userService.getUsersByIds(request.getIds()));
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> update(@Valid @RequestBody UserUpdateRequest request) {
        Boolean updated = userService.updateUserInfo(request);
        return BaseResponse.success("用户信息修改成功", updated);
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> delete() {
        Boolean result = userService.delete();
        return BaseResponse.success("删除成功",result);
    }

}
