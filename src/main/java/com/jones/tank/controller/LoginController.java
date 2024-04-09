package com.jones.tank.controller;

import com.jones.tank.entity.User;
import com.jones.tank.entity.param.*;
import com.jones.tank.object.ApplicationConst;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.service.UserService;
import com.jones.tank.util.LoginUtil;
import com.jones.tank.util.support.ValidMobile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(value = "注册/登录相关", tags = {"注册/登录相关"})
public class LoginController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("regist")
    public BaseResponse regist(@Validated @RequestBody @ApiParam(required=true) UserRegistParam param) {
        return userService.add(User.builder().mobile(param.getMobile()).password(param.getPassword()).userType(User.COMMON).build());
    }

    @ApiOperation(value = "手机号查重", notes = "手机号查重")
    @GetMapping("{mobile}/exists")
    public BaseResponse exists(@PathVariable (value="mobile") @ValidMobile @ApiParam String mobile) {
        return userService.mobileExists(mobile);
    }

    @ApiOperation(value = "登录Authorization检查", notes = "登录Authorization检查")
    @GetMapping("/auth/{auth}")
    public BaseResponse authExists(@PathVariable (value="auth") @ApiParam String auth) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", LoginUtil.getInstance().existsAuth(auth));
        return BaseResponse.builder().data(result).build();
    }

    @ApiOperation(value = "获取验证码", notes = "注册时获取验证码手机号可以为空，其他情况需要有手机号")
    @GetMapping("/verifyCode")
    public BaseResponse getVerifyCode(@RequestParam (value="mobile") @ValidMobile @ApiParam String mobile) {
        if (StringUtils.hasLength(mobile)) {
            return userService.getVerifyCode(mobile);
        } else {
            return BaseResponse.builder().data("HSE3").build();
        }
    }

//    @ApiOperation(value = "重置密码", notes = "重置密码")
//    @PostMapping("password/reset")
//    public BaseResponse passwordReset(@Validated @RequestBody @ApiParam(required=true) UserPasswordRestParam param) {
//        return userService.resetPassword(param);
//    }

    @ApiOperation(value = "登录", notes = "登录")
    @PostMapping("login")
    public BaseResponse login(@Validated @RequestBody @ApiParam(required=true) UserLoginParam param, HttpServletRequest request) {
        String appSource = request.getHeader(ApplicationConst.APP_SOURCE_FIELD);
        return userService.doLogin(param, appSource);
    }

    @ApiOperation(value = "小程序登录", notes = "登录")
    @PostMapping("wxlogin")
    public BaseResponse wxLogin(@Validated @RequestBody @ApiParam(required=true) UserWXLoginParam param) {
        return userService.doWxLogin(param);
    }

    @ApiOperation(value = "注销", notes = "注销")
    @PostMapping("logout")
    public BaseResponse logout() {
        LoginUtil.getInstance().removeUser();
        return BaseResponse.builder().build();
    }

}
