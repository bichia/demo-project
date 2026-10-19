package com.example.commodity.interceptor;

import com.example.commodity.controller.LoginController;
import com.example.commodity.entity.User;
import com.example.commodity.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 登录拦截器 + 角色访问控制 + 记住我 Cookie 自动登录
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /** 仅管理员可访问的管理路径前缀 */
    private static final Set<String> ADMIN_PATHS = Set.of(
            "/dashboard", "/products", "/categories", "/brands", "/admin"
    );

    private final UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(LoginController.SESSION_USER);

        // 会话失效时，尝试用记住我 Cookie 自动恢复登录（有效期 30 天）
        if (user == null) {
            user = restoreFromCookie(request);
            if (user != null) {
                request.getSession(true).setAttribute(LoginController.SESSION_USER, user);
            }
        }

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        // 角色控制：普通用户拦截管理路径
        if (!user.isAdmin()) {
            String uri = request.getRequestURI();
            String ctx = request.getContextPath();
            String path = uri.substring(ctx.length());
            for (String admin : ADMIN_PATHS) {
                if (path.equals(admin) || path.startsWith(admin + "/")) {
                    response.sendRedirect(ctx + "/shop");
                    return false;
                }
            }
        }
        return true;
    }

    /** 从 fruit_token Cookie 恢复用户，格式：URL编码用户名|sha256(密码) */
    private User restoreFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (!LoginController.REMEMBER_COOKIE.equals(c.getName()) || c.getValue() == null
                    || !c.getValue().contains("|")) {
                continue;
            }
            String[] parts = c.getValue().split("\\|", 2);
            try {
                String username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                return userService.loginByToken(username, parts[1]).orElse(null);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
