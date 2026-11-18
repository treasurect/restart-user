package com.treasure.restart.user.helper;

import com.treasure.common.response.BaseResponse;
import com.treasure.restart.user.entity.User;
import com.treasure.restart.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isEmpty()) {
            return unauthorized(response, "未登录");
        }

        if (!authorization.startsWith("Bearer ")) {
            return unauthorized(response, "Token格式错误");
        }

        try {
            String token = authorization.substring(7);
            Long userId = jwtUtils.getUserId(token);

            User user = userMapper.findByUserId(userId);

            if (user == null) {
                return unauthorized(response, "用户不存在");
            }

            if (user.getStatus() == 0) {
                return unauthorized(response, "账号已失效");
            }

            UserContext.setUserId(userId);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(response, "登录已过期，请重新登录");
        }
    }

    private boolean unauthorized(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            BaseResponse<?> result = BaseResponse.error(401, message);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            throw new IllegalStateException("响应认证失败信息时发生异常", e);
        }

        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserContext.clear();
    }
}
