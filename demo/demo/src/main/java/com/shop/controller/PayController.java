package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.entity.Order;
import com.shop.mapper.OrderMapper;
import com.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private OrderMapper orderMapper;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建 Stripe 收银台链接
     */
    @GetMapping("/create")
    public Result<String> createPay(@RequestParam String orderNo) {
        System.out.println("========== 收到支付请求，订单号: " + orderNo + " ==========");

        Order order = orderMapper.selectOne(
                new UpdateWrapper<Order>().eq("order_no", orderNo)
        );
        if (order == null) {
            System.out.println("❌ 订单不存在");
            return Result.error(400, "订单不存在");
        }
        if (order.getStatus() != 0) {
            System.out.println("❌ 订单状态不是待付款");
            return Result.error(400, "订单已支付或已取消");
        }

        try {
            long amount = order.getPayAmount().multiply(new java.math.BigDecimal("100")).longValue();
            System.out.println("💰 金额（分）: " + amount);

            URL url = new URL("https://api.stripe.com/v1/checkout/sessions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + stripeSecretKey.trim());
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            // success_url 和 cancel_url 必须整体 encode，否则 Stripe 会解析错参数
            String successUrl = "http://localhost:5173/orders?success=true&orderNo=" + orderNo + "&session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = "http://localhost:5173/orders";

            String body = "payment_method_types[0]=card"
                    + "&line_items[0][price_data][currency]=cny"
                    + "&line_items[0][price_data][unit_amount]=" + amount
                    + "&line_items[0][price_data][product_data][name]=" + urlEncode("商城订单 " + orderNo)
                    + "&line_items[0][quantity]=1"
                    + "&mode=payment"
                    + "&success_url=" + urlEncode(successUrl)
                    + "&cancel_url=" + urlEncode(cancelUrl)
                    + "&metadata[orderNo]=" + orderNo;

            System.out.println("📤 请求 Stripe API...");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("📥 Stripe 响应码: " + responseCode);

            StringBuilder response = new StringBuilder();
            if (responseCode >= 200 && responseCode < 300) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                    while (scanner.hasNextLine()) response.append(scanner.nextLine());
                }
            } else {
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name())) {
                    while (scanner.hasNextLine()) response.append(scanner.nextLine());
                }
                System.out.println("❌ Stripe 错误: " + response);
                return Result.error(500, "Stripe 错误: " + response.substring(0, Math.min(200, response.length())));
            }

            String json = response.toString();
            System.out.println("📄 返回长度: " + json.length());

            Map<String, Object> resultMap = objectMapper.readValue(json, Map.class);
            Object urlObj = resultMap.get("url");

            if (urlObj == null) {
                System.out.println("❌ Stripe 返回中无 url 字段");
                return Result.error(500, "Stripe 未返回支付链接");
            }

            String payUrl = urlObj.toString();
            System.out.println("✅ 支付链接: " + payUrl);
            return Result.success(payUrl);

        } catch (Exception e) {
            System.out.println("❌ 异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "创建支付失败: " + e.getMessage());
        }
    }

    /**
     * 支付成功确认：前端从 Stripe 跳回后，拿 session_id 来确认
     */
    @GetMapping("/confirm")
    public Result<String> confirmPay(@RequestParam String sessionId) {
        try {
            URL url = new URL("https://api.stripe.com/v1/checkout/sessions/" + sessionId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + stripeSecretKey.trim());
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            StringBuilder response = new StringBuilder();
            try (Scanner s = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                while (s.hasNextLine()) response.append(s.nextLine());
            }

            Map<String, Object> result = objectMapper.readValue(response.toString(), Map.class);
            String paymentStatus = (String) result.get("payment_status");

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
            String orderNo = metadata != null ? (String) metadata.get("orderNo") : null;

            System.out.println("🔔 Stripe 确认 session: " + sessionId + ", status: " + paymentStatus + ", orderNo: " + orderNo);

            if ("paid".equals(paymentStatus) && orderNo != null) {
                Order order = new Order();
                order.setStatus(1); // 1 = 待发货
                orderMapper.update(order,
                        new UpdateWrapper<Order>().eq("order_no", orderNo).eq("status", 0));
                System.out.println("✅ 订单 " + orderNo + " 已确认支付，状态改为待发货");
                return Result.success("PAID");
            }

            return Result.success("UNPAID");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "确认支付失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单支付状态
     */
    @GetMapping("/query")
    public Result<String> queryPay(@RequestParam String orderNo) {
        Order order = orderMapper.selectOne(
                new UpdateWrapper<Order>().eq("order_no", orderNo)
        );
        if (order == null) return Result.error(400, "订单不存在");
        return Result.success(order.getStatus() == 1 ? "PAID" : "UNPAID");
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}