package com.treasure.restart.user.service;

import com.treasure.restart.user.dto.UserInfoResponse;
import com.treasure.restart.user.dto.UserLoginRequest;
import com.treasure.restart.user.dto.UserLoginResponse;
import com.treasure.restart.user.dto.UserUpdateRequest;
import com.treasure.restart.user.dto.UserSimpleVO;

import java.util.List;

public interface UserService {

    UserLoginResponse loginPwd(UserLoginRequest request);

    UserInfoResponse getUserInfo();

    UserSimpleVO getUserById(Long id);

    List<UserSimpleVO> getUsersByIds(List<Long> ids);

    Boolean updateUserInfo(UserUpdateRequest request);

    Boolean delete();

    Boolean delete(Long id);
}
