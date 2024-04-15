package com.jones.tank.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html
 * Created by jones on 19-9-2.
 */
@Component
@Slf4j
public class WechatWeProgramUtil {
    public static final String WECHAT_API_URL_BASE = "https://api.weixin.qq.com";
    public static String WECHAT_WEPROGRAM_APP_ID;
    private static String WECHAT_WEPROGRAM_APP_SECRET;
    private static String URL_CODE_TO_SESSION;

    @PostConstruct
    private void init(){
        URL_CODE_TO_SESSION = String.format(WECHAT_API_URL_BASE + "/sns/jscode2session?grant_type=authorization_code&appid=%s&secret=%s&js_code=", WECHAT_WEPROGRAM_APP_ID, WECHAT_WEPROGRAM_APP_SECRET);
    }

    /**
     * 　code 转化为sessionKey
     *
     * @param code
     */
    public static JSONObject getSessionKey(String code) {
        try {
            JSONObject resp = HttpClientUtil.getJson(URL_CODE_TO_SESSION + code);
            log.info("wechat code to session key resp: " + resp.toJSONString());
            return resp;
        } catch (Exception e) {
            log.error("fail to get wechat session key", e);
        }
        return null;
    }

    /**
     * 获取微信方的用户信息
     *
     * @param sessionInfo
     * @param encrypedData
     * @param iv
     * @return
     */
    public static Map<String, String> getUserInfo(Map<String, Object> sessionInfo, String encrypedData, String iv) {
        log.info("start to parse wx user info: \\n sessionInfo:{}\\n encryptedData:{}\\n iv:{}", sessionInfo, encrypedData, iv);
        Map<String, String> resultMap = null;
        String sessionKey = String.valueOf(sessionInfo.get("session_key"));
        if (StringUtils.hasLength(sessionKey)) {
            JSONObject decryptedResult = getDecryptedUserInfo(sessionKey, encrypedData, iv);
            String openid = sessionInfo.get("openid").toString();
            String unionid = sessionInfo.getOrDefault("unionid", "").toString();
            if (WECHAT_WEPROGRAM_APP_ID.equals(decryptedResult.getJSONObject("watermark").getString("appid"))) {
                resultMap = new HashMap<>();
                resultMap.put("mobile", decryptedResult.getString("purePhoneNumber"));
                resultMap.put("openid", openid);
                resultMap.put("unionid", unionid);
            } else {
                log.error("either sessionInfo({}) or encrypedData({}) or iv({}) is invalid", sessionInfo, encrypedData, iv);
            }
        }
        return resultMap;
    }

    public static JSONObject getDecryptedUserInfo(String sessionKey, String encryptedData, String iv) {
        try {
            log.info("start to decrypt user info:  \n sessionKey:{}\n encryptedData:{}\n iv:{}", sessionKey, encryptedData, iv);
            String result = AesCbcUtil.decrypt(encryptedData, sessionKey, iv, "UTF-8");
            JSONObject decryptedResult = JSONObject.parseObject(result);
            log.info("succeed in decrypt wx user info: \\n {}", JSONObject.toJSONString(decryptedResult));
            return decryptedResult;
        } catch (Exception e) {
            log.error("fail to decrypt mobile");
        }
        return null;
    }

    /**
     * {"phoneNumber":"13122900225","watermark":{"appid":"wxff2a14ab973dffe0","timestamp":1599702528},"purePhoneNumber":"13122900225","countryCode":"86"}
     */
    public static void main(String[] args) {
        String code = "0117nWkl2CCWz541Wqnl2NnG5o37nWkK";
        String encryptedData = "jxme4Eg3tWEA0EobDQTfMvfzfhEqSK0Rqat49Vzo3W70WRemby6nK3UcDPZ5PeQgPpvtvsUoMvtJWSbH3NqLy40gXPcGBWsehPCiVu53X3wTMqi1ByyGVel8nXsMW1Il0l8afTQiyotSnbExu/9EfCevP7tUPnXfSSt/KMLFqBYQ7EvU8o/Or/4NSobIDZqoxtDJRPUp7fX6BzvB3iTzxiXcdR7YiOLPR+8fegnXngzwwVFWFpAeUrbo/C8WRgFuLGBdS4601ru04smlgXld+ekfZhO3+FXHx/41z9jjfi/WAhcSBZ9e4PHsF7THnzwqhBd/Xm7P1Ufd5ZsPJ+/UxLw1LbnvgGAkUHey7knBB/yXrrAyDhVuT7+afyYMPtjjQFhb5EEoc/RvaN0417dIXPnST/ggVaZH9X9NI2uBEvNB2r0YGVxQdB759QzAbLTCR0DEPiescIgSIezKAqmtmJM02yw8+VHiQIGl2TLJxts=";
        String iv = "4+5ZrMc/Fyd42cjMSGmN2g==";
//        System.out.println(JSONObject.toJSONString(getSessionKey("0018MKFa1OEWAz0tKRIa1jbapn48MKFc")));
        String sessionKey = "v0+5YOV/yfWP4dOlw3gkAQ==";
        encryptedData = "7Ch6Sfw7zGAedjC8s//7iY6raFnIyj81yb1iPZfkmCg1Q7Ra+va1xD+iz81+OM5LoaUb3fva105WsieYtBhx0Gi5VO1djYYWsaKdC06nJphWFUW65kiqufkg2fzkJ2mrsnv2ptmqp61Lk/SLdiVXSvCNEDYE4+TbTlXlPhQAQ73Mmjzgg/ijPc2v9NulOKm9ePrgiN0t9WkKQGl7T7mE4Q==";
        iv = "bVH5hTzu0LDX7bTfcTAU1g==";
//        System.out.println(JSONObject.toJSONString(getDecryptedUserInfo(sessionKey, encryptedData,iv)));
//        WechatApiUtil util = new WechatApiUtil();

        JSONObject decryptedResult = getDecryptedUserInfo(sessionKey, encryptedData, iv);
//            String unionid = sessionInfo.getString("unionid");
        if ("wxff2a14ab973dffe0".equals(decryptedResult.getJSONObject("watermark").getString("appid")))
            System.out.println(decryptedResult.getString("purePhoneNumber"));
//                resultMap.put("unionid", unionid);

//        System.out.println(JSONObject.toJSONString(util.getUserInfo(code,encryptedData,iv)));
    }

    @Value("${wechat.weprogram.app.id:}")
    private void setAppId(String appId){
        WechatWeProgramUtil.WECHAT_WEPROGRAM_APP_ID = appId;
    }
    @Value("${wechat.weprogram.app.secret:}")
    private void setAppSecret(String appSecret){
        WechatWeProgramUtil.WECHAT_WEPROGRAM_APP_SECRET = appSecret;
    }

}


