package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.config.SmsCodeCache;
import com.shop.entity.User;
import com.shop.mapper.UserMapper;
import com.shop.service.impl.SmsService;
import com.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SmsCodeCache smsCodeCache;

    // 原有登录接口
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        User dbUser = userMapper.selectOne(wrapper);
        if (dbUser == null) {
            return Result.error(400, "用户不存在");
        }
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return Result.error(400, "密码错误");
        }
        return Result.success(dbUser);
    }

    // 原有注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error(400, "手机号已注册");
        }
        userMapper.insert(user);
        return Result.success(user);
    }

    // ========== 新增：发送短信验证码 ==========
    @PostMapping("/sendCode")
    public Result sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.length() != 11) {
            return Result.error(400, "手机号格式错误");
        }

        String code = smsCodeCache.generateAndSet(phone);

        // 开发阶段先打印到控制台，等AppCode配好了再打开真实发送
        System.out.println("【短信验证码】手机号: " + phone + " 验证码: " + code);

        // 真实发送（配好AppCode后取消下面注释）
        // boolean success = smsService.sendVerifyCode(phone, code);
        // if (!success) {
        //     return Result.error(500, "短信发送失败，请稍后重试");
        // }

        return Result.success("发送成功");
    }

    // ========== 新增：重置密码 ==========
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String password = params.get("password");

        if (!smsCodeCache.verify(phone, code)) {
            return Result.error(400, "验证码错误或已过期");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error(400, "该手机号未注册");
        }

        user.setPassword(password);
        userMapper.updateById(user);
        smsCodeCache.remove(phone);

        return Result.success("密码重置成功");
    }

    @GetMapping("/info")
    public Result info(@RequestParam Long id) {
        User user = userMapper.selectById(id);
        return Result.success(user);
    }
}