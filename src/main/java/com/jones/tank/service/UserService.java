package com.jones.tank.service;

import com.alibaba.fastjson.JSONObject;
import com.jones.tank.entity.User;
import com.jones.tank.entity.param.UserLoginParam;
import com.jones.tank.entity.param.UserPasswordRestParam;
import com.jones.tank.entity.param.UserWXLoginParam;
import com.jones.tank.entity.query.UserQuery;
import com.jones.tank.object.ApplicationConst;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.CustomServiceImpl;
import com.jones.tank.object.ErrorCode;
import com.jones.tank.repository.UserMapper;
import com.jones.tank.util.*;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jones.tank.util.WechatWeProgramUtil.getSessionKey;

@Slf4j
@Service
public class UserService extends CustomServiceImpl<UserMapper, User> {

    @Autowired
    private UserMapper mapper;

    private boolean exists(String mobile){
        Long count = mapper.findCount(UserQuery.builder().mobile(mobile).build());
        return count > 0;
    }

    public BaseResponse personal(Long userId){
        User user = mapper.findById(userId);
        return BaseResponse.builder().data(user).build();
    }

    /**
     * 手机号查重
     * @param mobile
     * @return
     */
    public BaseResponse mobileExists(String mobile){
        Map<String, Object> result =new HashMap<>();
        result.put("exists", true);
        if(exists(mobile)){
            return BaseResponse.builder().code(ErrorCode.REGIST_MOBILE_EXISTS).data(result).build();
        }else {
            result.put("exists", false);
            return BaseResponse.builder().data(result).build();
        }
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse add(User user){
        if(!exists(user.getMobile())){
            if(!StringUtils.isEmpty(user.getPassword())){
                // TODO 对密码加密
                user.setPassword(user.getPassword());
            }
            if(user.getUserType() == null){
                user.setUserType(User.COMMON);
            }
            mapper.insert(user);
//            Long userId = user.getId();
//            user.setId(null);
//            user.setUserId(userId);
//            user.setSgname("新用户" + user.getMobile().substring(user.getMobile().length()-4));
//            mapper.insertProfile(user);
            return BaseResponse.builder().data(user.getUserId()).build();
        } else {
            return BaseResponse.builder().code(ErrorCode.REGIST_MOBILE_EXISTS).build();
        }
    }

    /**
     * 获取验证码
     * TODO 对接短信系统
     * @param mobile
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse getVerifyCode(String mobile){
        String verifyCode = RandomString.generateVerifyCode();
//        AliMnsSender.sendMns(mobile, verifyCode);
//        return BaseResponse.builder().build();
        List<User> users = mapper.findList(UserQuery.builder().mobile(mobile).build());
        if(users.size() == 1){
            User user_db = users.get(0);
            User user_update = User.builder().verifyCode(verifyCode).build();
            user_update.setUserId(user_db.getUserId());
            mapper.update(user_update);
            AliMnsSender.sendMns(mobile, verifyCode);
            return BaseResponse.builder().build();
        } else if(users.size() < 1){
            return BaseResponse.builder().code(ErrorCode.LOGIN_MOBILE_NOTEXISTS).build();
        } else {
            throw new InternalException("手机号重复");
        }
    }

    /**
     * 重置密码
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse resetPassword(UserPasswordRestParam param){
        User user = mapper.findOneByMobile(param.getMobile());
        if(user == null){
            return BaseResponse.builder().code(ErrorCode.LOGIN_MOBILE_NOTEXISTS).build();
        }
        if(param.getVerifyCode() != null && param.getVerifyCode().equals(user.getVerifyCode())) {
            User user_update = User.builder().password(param.getPassword()).build();
            user_update.setUserId(user.getUserId());
            mapper.update(user_update);
            return BaseResponse.builder().data(user_update.getUserId()).build();
        } if(param.getPasswordOld() != null && param.getPasswordOld().equals(user.getPassword())) {
            User user_update = User.builder().password(param.getPassword()).build();
            user_update.setUserId(user.getUserId());
            mapper.update(user_update);
            return BaseResponse.builder().data(user_update.getUserId()).build();
        } else{
            return BaseResponse.builder().code(ErrorCode.VERIFY_CODE_FAILED).build();

        }
    }

    /**
     * 用户登录
     * @param userParam
     * @return
     */
    public BaseResponse doLogin(UserLoginParam userParam, String appSource) {
        UserQuery.UserQueryBuilder builder = UserQuery.builder().mobile(userParam.getMobile());
        if(!StringUtils.hasLength(userParam.getVerifyCode()) && !StringUtils.hasLength(userParam.getPassword())) {
            return BaseResponse.builder().code(ErrorCode.VALIDATION_FAILED).message("验证码和密码不能同时为空").build();
        } else if (StringUtils.hasLength(userParam.getVerifyCode())){
            builder.verifyCode(userParam.getVerifyCode()).password(null);
        } else {
                builder.password(userParam.getPassword()).verifyCode(null);
        }
        List<User> users = mapper.findList(builder.build());
        if(users.size() == 1){
            return login(users.get(0), appSource);
        } else if(users.size() < 1){
            return BaseResponse.builder().code(ErrorCode.LOGIN_FAIL).build();
        } else {
            throw new InternalException("手机及验证码重复");
        }
    }
    public BaseResponse innerLogin(Long userId, String appSource){
        User user = mapper.findById(userId);
        return login(user, appSource);
    }
    public BaseResponse login(User user, String appSource){
        Date now = new Date();
//        User user_db = users.get(0);
        user.setLastLoginTime(now);
        User user_update = User.builder().userId(user.getUserId()).lastLoginTime(now).build();
        mapper.update(user_update);
        Map<String, Object> result = new HashMap<>();
        String authorization = UuidUtil.generate().toUpperCase();
        result.put("id", user.getUserId());
        // 返回用户基本信息
        // 从enterprise_user表中查询所有的企业
        // 如果是管理员则不返回
        log.info("userparam appsource: " + appSource);
        if(user.getUserType().equals(User.COMMON) && ApplicationConst.APP_SOURCE_ADMIN.equals(appSource)) {
            // 普通用户登录后台要拒绝
            log.info("当前用户%s,　为普通用户无权限登录后台管理");
            return BaseResponse.builder().code(ErrorCode.ADMIN_LOGIN_DENIED).build();
        }
//        result.put("expireTime", new Date(now.toInstant(ZoneOffset.UTC).toEpochMilli() + LoginUtil.COOKIE_MAX_INACTIVE_INTERVAL));
        result.put("expireTime", new Date().getTime() + LoginUtil.COOKIE_MAX_AGE);
        result.put("userType", user.getUserType());
        result.put("authorization", authorization);
        LoginUtil.getInstance().setUser(authorization, user);
        return BaseResponse.builder().data(result).build();
    }
    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全
    private ConcurrentHashMap<String, Map<String, Object>> weprogramSessionInfo = null;
    private static Long WEPROGRAM_SESSION_INFO_EXPIRES = 5*60*1000l;


    public BaseResponse doWxLogin(UserWXLoginParam param){
        User user = null;
        if(!StringUtils.hasLength(param.getEncryptedData())) {
            JSONObject result = WechatWeProgramUtil.getSessionKey(param.getCode());
            if(!result.containsKey("openid")){
                return BaseResponse.builder().code(ErrorCode.INTERNAL_ERROR).data(result).build();
            }
            user = mapper.findOne(UserQuery.builder().openid(result.getString("openid")).build());
            if(user==null){
                result.put("timestamp", new Date().getTime());
                getWeprogramSessionInfo().put(param.getCode(), result);
                return BaseResponse.builder().code(ErrorCode.WECHAT_OPENID_NOTEXISTS).build();
            }
            user.setPassword(result.getString("openid"));
        } else {
            Map<String, String> wechatInfo = WechatWeProgramUtil.getUserInfo(getWeprogramSessionInfo().get(param.getCode()),param.getEncryptedData(), param.getIv());
            if(wechatInfo == null) {
                return BaseResponse.builder().code(ErrorCode.WECHAT_LOGIN_VERIFY_FAIL).build();
            }
            user = mapper.findOneByMobile(wechatInfo.get("mobile"));
            if(user == null){
                user = User.builder().mobile(wechatInfo.get("mobile")).userType(User.COMMON).password(wechatInfo.get("openid")).openid(wechatInfo.get("openid")).unionid(wechatInfo.get("unionid")).build();
                add(user);
            } else if (StringUtils.hasLength(user.getOpenid()) || StringUtils.hasLength(user.getUnionid())) {
                mapper.update(User.builder().userId(user.getUserId()).password(wechatInfo.get("openid")).openid(wechatInfo.get("openid")).unionid(wechatInfo.get("unionid")).build());
            }
        }
        return login(user, ApplicationConst.APP_SOURCE_WEIXIN);
    }

    private Map<String, Map<String, Object>> getWeprogramSessionInfo(){
        if(weprogramSessionInfo == null){
            synchronized (syncLock){
                if(weprogramSessionInfo == null){
                    weprogramSessionInfo = new ConcurrentHashMap<>();
                    ScheduledExecutorService monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            for(Map.Entry<String, Map<String, Object>> entry: weprogramSessionInfo.entrySet()){
                                if(new Date().getTime() - Long.parseLong(entry.getValue().get("timestamp").toString()) > WEPROGRAM_SESSION_INFO_EXPIRES){
                                    weprogramSessionInfo.remove(entry.getKey());
                                }
                            }
                        }
                    }, WEPROGRAM_SESSION_INFO_EXPIRES, WEPROGRAM_SESSION_INFO_EXPIRES, TimeUnit.MILLISECONDS);
                }
            }
        }
        return weprogramSessionInfo;
    }


}

