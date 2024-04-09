package com.jones.tank.controller;

import com.jones.tank.object.BaseResponse;
import com.jones.tank.util.WechatWeProgramUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/wechat"})
@Profile({"wechat"})
@Api(value = "微信接口", tags = {"微信接口"})
public class WechatController {

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

}
