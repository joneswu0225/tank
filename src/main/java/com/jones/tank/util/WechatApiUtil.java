package com.jones.tank.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sun.misc.IOUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jones on 19-9-2.
 */
@Component
@Slf4j
//@Profile({"wechat"})
public class WechatApiUtil {
    public static String appId;
    private static String appSecret;
    private static String accessToken;
    private String jsTicket;
    public static final String WECHAT_API_URL_BASE = "https://api.weixin.qq.com";
    public static String URL_GET_ACCESS_TOKEN = WECHAT_API_URL_BASE + "/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    public static String URL_GET_JS_TICKET = WECHAT_API_URL_BASE + "/cgi-bin/ticket/getticket?type=jsapi&access_token=%s";
    public static String URL_GET_MEDIA = WECHAT_API_URL_BASE + "/cgi-bin/media/get?access_token=%s&media_id=%s";
    private static String fileUploadPath = "";


    @PostConstruct
    public void init(){
        URL_GET_ACCESS_TOKEN = String.format(URL_GET_ACCESS_TOKEN, appId, appSecret);
        generateAccessToken();
        generateJsTicket();
    }

    public String getAccessToken(){
        return WechatApiUtil.accessToken;
    }

    public String getJsTicket(){
        if(StringUtils.isEmpty(jsTicket)){
            generateJsTicket();
        }
        return jsTicket;
    }

    public Map<String, Object> getJsSdkSign(String url){
        Map<String, Object> result = Sign.sign(jsTicket, url);
        result.put("appid", appId);
        result.put("access_token", getAccessToken());
        return result;
    }

    public static String getMediaBase64(String mediaId){
        String mediaUrl = String.format(URL_GET_MEDIA, accessToken, mediaId);
        log.info("start to get voice info from [ " + mediaUrl + " ]");
        String result = null;
        try{
//            String uuid = UUID.fromString(mediaId).toString();
            URL url = new URL(mediaUrl);
            InputStream inStream = url.openConnection().getInputStream();
            byte[] bytes = IOUtils.readFully(inStream, 0, true);
            result = Base64.getEncoder().encodeToString(bytes);
        }catch (Exception e){
            log.error("fail to download wechat files", e);
        }
        return result;
    }

//    @Scheduled(cron = "* * 0/1 * * ?")
    @Scheduled(fixedRate = 3600000)
    private void generateAccessToken() {
        try {
            JSONObject resp = HttpClientUtil.getJson(URL_GET_ACCESS_TOKEN);
            log.info("wechat access token resp: " + resp.toJSONString());
            WechatApiUtil.accessToken = resp.getString("access_token");
        } catch (Exception e){
            log.error("fail to get wechat access token", e);
        }
    }

//    @Scheduled(cron = "* * 0/2  * * ?")
    @Scheduled(fixedRate = 7000000)
    private void generateJsTicket(){
        try {
            JSONObject resp = HttpClientUtil.getJson(String.format(URL_GET_JS_TICKET, this.accessToken));
            log.info("wechat js ticker resp: " + resp.toJSONString());
            this.jsTicket = resp.getString("ticket");
        } catch (Exception e){
            log.error("fail to get wechat js ticket", e);
        }
    }

    @Value("${wechat.official.app.id:}")
    private void setAppId(String appId){
        WechatApiUtil.appId = appId;
    }
    @Value("${wechat.official.app.secret:}")
    private void setAppSecret(String appSecret){
        WechatApiUtil.appSecret = appSecret;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String aa = "https://msa.vr2shipping.com/my/p_ditor/0/248/2";
        System.out.println(URLDecoder.decode(aa, "UTF-8"));
//        String toolPath = "/usr/local/ffmpeg/bin/ffmpeg";
//        String sourcePath = "/home/test4.amr";
//        String targetPath = "/home/test4.wav";
//        changeToWav(sourcePath, targetPath);
//        changeToWav("/home/jones/test2.amr", "/home/jones/test2.wav",1000.0f);
    }
}


class Sign {
    public static Map<String, Object> sign(String jsapi_ticket, String url) {
        Map<String, Object> ret = new HashMap<String, Object>();
        String nonce_str = create_nonce_str();
        Long timestamp = create_timestamp();
        String string1;
        String signature = "";
        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("noncestr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    private static String create_nonce_str() {
//        return UUID.randomUUID().toString();
        return Md5Util.md5(String.valueOf(System.currentTimeMillis()));
    }
    private static Long create_timestamp() {
        return System.currentTimeMillis() / 1000;
    }
}

