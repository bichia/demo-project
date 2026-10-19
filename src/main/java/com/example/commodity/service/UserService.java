package com.example.commodity.service;

import com.example.commodity.entity.User;
import com.example.commodity.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户名 + 密码校验，成功返回用户，失败返回 empty
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim())
                .filter(u -> u.getPassword().equals(password));
    }

    /**
     * 令牌登录（记住我 / 免启动登录页）：
     * token = sha256(密码) 的十六进制串，校验通过返回用户
     */
    public Optional<User> loginByToken(String username, String token) {
        if (username == null || token == null || token.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim())
                .filter(u -> sha256Hex(u.getPassword()).equalsIgnoreCase(token));
    }

    /**
     * SHA-256 十六进制摘要
     */
    public static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xf, 16));
                sb.append(Character.forDigit(b & 0xf, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
