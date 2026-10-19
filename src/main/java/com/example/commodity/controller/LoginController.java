package com.example.commodity.controller;

import com.example.commodity.entity.User;
import com.example.commodity.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginController {

    public static final String SESSION_USER = "loginUser";

    /** 记住我 Cookie：30 天免登录 */
    public static final String REMEMBER_COOKIE = "fruit_token";
    public static final int REMEMBER_MAX_AGE = 30 * 24 * 3600;

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String captcha,
                          @RequestParam(required = false) String remember,
                          HttpSession session,
                          HttpServletResponse response,
                          Model model) {
        // 验证码校验
        String savedCaptcha = (String) session.getAttribute("captchaCode");
        if (savedCaptcha == null || captcha == null || !savedCaptcha.equalsIgnoreCase(captcha.trim())) {
            model.addAttribute("error", "验证码错误");
            model.addAttribute("username", username);
            return "login";
        }
        session.removeAttribute("captchaCode");

        return userService.login(username, password)
                .map(user -> {
                    session.setAttribute(SESSION_USER, user);
                    if (remember != null) {
                        // 30 天免登录 Cookie：用户名 + sha256(密码)
                        String value = URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8)
                                + "|" + UserService.sha256Hex(user.getPassword());
                        Cookie cookie = new Cookie(REMEMBER_COOKIE, value);
                        cookie.setMaxAge(REMEMBER_MAX_AGE);
                        cookie.setPath("/");
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie);
                    }
                    return user.isAdmin() ? "redirect:/dashboard" : "redirect:/shop";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "用户名或密码错误");
                    model.addAttribute("username", username);
                    return "login";
                });
    }

    /**
     * 免启动登录页快捷入口：
     * /auto-login?u=用户名&k=sha256(密码)，校验通过直接建立会话
     */
    @GetMapping("/auto-login")
    public String autoLogin(@RequestParam String u,
                            @RequestParam String k,
                            HttpSession session) {
        return userService.loginByToken(u, k)
                .map(user -> {
                    session.setAttribute(SESSION_USER, user);
                    return user.isAdmin() ? "redirect:/dashboard" : "redirect:/shop";
                })
                .orElse("redirect:/login");
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie(REMEMBER_COOKIE, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
