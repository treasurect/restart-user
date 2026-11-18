package com.treasure.restart.user.service;

import com.treasure.restart.user.dto.UserInfoResponse;
import com.treasure.restart.user.dto.UserLoginRequest;
import com.treasure.restart.user.dto.UserLoginResponse;
import com.treasure.restart.user.dto.UserUpdateRequest;

public interface UserService {

    UserLoginResponse loginPwd(UserLoginRequest request);

    UserInfoResponse getUserInfo();

    Boolean updateUserInfo(UserUpdateRequest request);

    Boolean delete();

    Boolean delete(Long id);
}
