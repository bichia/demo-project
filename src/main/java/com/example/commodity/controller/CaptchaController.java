package com.example.commodity.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

/**
 * 图形验证码控制器
 */
@Controller
public class CaptchaController {

    private static final String CAPTCHA_SESSION_KEY = "captchaCode";
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Random RANDOM = new Random();

    @GetMapping("/captcha")
    public void captcha(HttpSession session, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(245, 247, 251));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 生成验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(RANDOM.nextInt(180), RANDOM.nextInt(180), RANDOM.nextInt(180)));
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                       RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }

        // 噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
            g.fillRect(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 1, 1);
        }

        // 字符
        String[] colors = {"#4f46e5", "#ea580c", "#059669", "#dc2626", "#7c3aed"};
        for (int i = 0; i < CODE_LENGTH; i++) {
            g.setColor(Color.decode(colors[RANDOM.nextInt(colors.length)]));
            g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24 + RANDOM.nextInt(6)));
            int x = 10 + i * 26;
            int y = 28 + RANDOM.nextInt(6) - 3;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();

        // 存入 session
        session.setAttribute(CAPTCHA_SESSION_KEY, code.toString());

        // 输出图片
        try (OutputStream os = response.getOutputStream()) {
            ImageIO.write(image, "png", os);
            os.flush();
        }
    }
}
