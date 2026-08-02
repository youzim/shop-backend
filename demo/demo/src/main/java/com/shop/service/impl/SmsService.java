package com.shop.service.impl;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SmsService {

    // ========== 开关 ==========
    // true = 强制开发模式，只打印不发短信（现在先用这个）
    // false = 尝试真实发送，失败自动降级打印
    private static final boolean FORCE_DEV_MODE = true;

    private static final String APPCODE = "bcd87cdb0c364b899bda3dad84bb85b3";
    private static final String SMS_URL = "https://cxkjsms.market.alicloudapi.com/chuangxinsms/dxjk";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendVerifyCode(String phone, String code) {
        // 强制开发模式：直接打印，不发短信
        if (FORCE_DEV_MODE) {
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  【开发模式】短信验证码未真实发送，请在控制台查看验证码      ║");
            System.out.println("║  手机号: " + phone + "                                   ║");
            System.out.println("║  验证码: " + code + "                                    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            return true;
        }

        // 尝试真实发送
        try {
            String content = URLEncoder.encode(
                    "【创信】你的验证码是：" + code + "，5分钟内有效，请勿泄露！",
                    StandardCharsets.UTF_8
            );

            String url = UriComponentsBuilder.fromHttpUrl(SMS_URL)
                    .queryParam("content", content)
                    .queryParam("mobile", phone)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + APPCODE);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            System.out.println("【创信短信】状态码: " + response.getStatusCode());
            System.out.println("【创信短信】返回体: " + response.getBody());

            // 如果 HTTP 200 且返回体包含成功标识，才算真正成功
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().contains("\"code\":\"0\"")) {
                return true;
            }

            // 真实发送失败，自动降级到控制台打印
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  【发送失败，自动降级】短信平台返回错误，请在控制台查看验证码 ║");
            System.out.println("║  手机号: " + phone + "                                   ║");
            System.out.println("║  验证码: " + code + "                                    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            return true; // 返回 true 让前端正常走流程

        } catch (Exception e) {
            // 异常也降级
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  【异常降级】短信发送异常，请在控制台查看验证码              ║");
            System.out.println("║  手机号: " + phone + "                                   ║");
            System.out.println("║  验证码: " + code + "                                    ║");
            System.out.println("║  异常信息: " + e.getMessage());
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            return true;
        }
    }
}