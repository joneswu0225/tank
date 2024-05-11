package com.jones.tank.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.param.UserRegistParam;
import com.jones.tank.entity.param.WeprogramMsgCheckParam;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.ErrorCode;
import com.jones.tank.util.WechatApiUtil;
import com.jones.tank.util.WechatWeProgramUtil;
import com.jones.tank.util.LoginUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping({"/wechat"})
//@Profile({"wechat"})
@Api(value = "微信接口", tags = {"微信接口"})
public class WechatController {
    @Autowired
    private WechatApiUtil wechatApiUtil;
    @Autowired
    private LoginUtil loginUtil;
    /**
     * 获取accessToken
     * @return
     */
    @ApiOperation(value = "获取accessToken", notes = "获取accessToken")
    @GetMapping(value="/accessToken")
    public BaseResponse getAccessToken(){
        return BaseResponse.builder().data(wechatApiUtil.getAccessToken()).build();
    }
    @ApiOperation(value = "获取accessToken", notes = "获取accessToken")
    @GetMapping(value="/accessToken/refresh")
    public BaseResponse refreshAccessToken(){
        return BaseResponse.builder().data(wechatApiUtil.generateAccessToken()).build();
    }
    /**
     * 获取accessToken
     * @return
     */
    @ApiOperation(value = "获取accessToken", notes = "获取accessToken")
    @GetMapping(value="/jsTicket")
    public BaseResponse getJsTicket(){
        return BaseResponse.builder().data(wechatApiUtil.getJsTicket()).build();
    }
    /**
     * 获取accessToken
     * @return
     */
    @ApiOperation(value = "获取jsSDKSign", notes = "获取jsSDKSign")
    @GetMapping(value="/sign")
    public BaseResponse getJsSdkSign(@NotBlank(message = "url不能为空") @RequestParam(name="url", required = true) String url){
        return BaseResponse.builder().data(wechatApiUtil.getJsSdkSign(url)).build();
    }
    /**
     * 获取accessToken
     * @return
     */
    @ApiOperation(value = "获取jsSDKSign", notes = "获取jsSDKSign")
    @GetMapping(value="/media")
    public BaseResponse getMedia(@RequestParam(name="mediaId", required = true) String mediaId){
        return BaseResponse.builder().data(wechatApiUtil.getMediaBase64(mediaId)).build();
    }

    /**
     * 小程序Code2SessionKey
     * @return
     */
    @ApiOperation(value = "小程序Code2SessionKey", notes = "小程序Code2SessionKey")
    @GetMapping(value="/weprogram/wxCode2SessionKey")
    public BaseResponse wxCode2SessionKey(@PathVariable String weprogramId, @RequestParam(name="code", required = true) String code){
        return BaseResponse.builder().data(WechatWeProgramUtil.getSessionKey(code)).build();
    }
    /**
     * 小程序wxDecryptedUserInfo
     * @return
     */
    @ApiOperation(value = "小程序wxDecryptedUserInfo", notes = "小程序wxDecryptedUserInfo")
    @GetMapping(value="/weprogram/wxDecryptedUserInfo")
    public BaseResponse wxDecryptedUserInfo(@RequestParam(name="sessionKey", required = true) String sessionKey,
                                            @RequestParam(name="encryptedData", required = true) String encryptedData,
                                            @RequestParam(name="iv", required = true) String iv){
        return BaseResponse.builder().data(WechatWeProgramUtil.getDecryptedUserInfo(sessionKey, encryptedData,iv)).build();
    }
    /**
     * 小程序appid
     * @return
     */
    @ApiOperation(value = "小程序appid", notes = "小程序appid")
    @GetMapping(value="/weprogram/wxAppid")
    public BaseResponse wxAppid(){
        return BaseResponse.builder().data(WechatWeProgramUtil.WECHAT_WEPROGRAM_APP_ID).build();
    }

    /**
     * 小程序appid
     * @return
     */
    @ApiOperation(value = "小程序内容检测", notes = "小程序内容检测")
    @PostMapping(value="/weprogram/msgCheck")
    public BaseResponse msgCheck(@Validated @RequestBody @ApiParam(required=true) WeprogramMsgCheckParam param) {
        String openid = param.getOpenid();
        if(StringUtils.isEmpty(openid)){
            openid = loginUtil.getInstance().getUser().getOpenid();
        }
        if(StringUtils.isEmpty(openid)){
            return BaseResponse.builder().code(ErrorCode.INTERNAL_ERROR).message("当前用户未绑定微信小程序").build();
        }
        return BaseResponse.builder().data(WechatApiUtil.checkMsgSec(openid, param.getContent(), null)).build();
    }

}
