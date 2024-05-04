package com.jones.tank.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.User;
import com.jones.tank.object.ApplicationConst;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginUtil {
    public static final String APP_AUTH = "authorization";
    public static final String USER_MOBILE = "mobile";
    public static final String USER_ID = "userId";
    public static String APP_DOMAIN = ApplicationConst.APP_DOMAIN;
    public static LoginUtil INSTANCE = null;
    private ConcurrentHashMap<String, User> loginUser = new ConcurrentHashMap<>();

    public static LoginUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (LoginUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoginUtil();
                    INSTANCE.setLoginUser("mastertoken", User.builder().userId(1l).mobile("admin").password("admin").build());
                    INSTANCE.setLoginUser("35C1DB79E47946F1BF0C042E86E92FFF", User.builder().userId(29l).mobile("18616701071").password("o7rt-6z4aBA8kP1O1pYUZDdiFEQM").build());
                    INSTANCE.setLoginUser("35C1DB79E47946F1BF0C042E86E92FF1", User.builder().userId(30l).mobile("13564332436").password("o7rt-6zyvJZrN8uay5jrTvwEwXRw").build());
                }
            }
        }
        return INSTANCE;
    }

    public boolean existsAuth(String auth){
        return loginUser.containsKey(auth);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getResponse();
    }

    public User getUser() {
        String auth = getRequest().getHeader(APP_AUTH);
//        if(getRequest().getCookies() != null) {
//            Optional<Cookie> cookieAuth = Arrays.asList(getRequest().getCookies()).stream().filter(p -> p.getName().equals(APP_AUTH)).findAny();
//            if (cookieAuth.isPresent()) {
//                auth = cookieAuth.get().getValue();
//            }
//        }
        return getLoginUser(auth);
    }
//    public User getUser() {
//        String auth = getRequest().getHeader(APP_AUTH);
//        if(StringUtils.isNotBlank(auth)){
//            if(getRequest().getCookies()==null){
//                refreshLoginUser(auth);
//            } else {
//                Optional<Cookie> cookieAuth = Arrays.stream(getRequest().getCookies()).filter(p -> p.getName().equals(APP_AUTH)).findAny();
//                if(!cookieAuth.isPresent()){
//                    refreshLoginUser(auth);
//                } else {
//                    auth = cookieAuth.get().getValue();
//                }
//            }
//        }
//        return getLoginUser(auth);
//    }

    public Long getLoginUserId(){
        User user = getUser();
        if(user == null){
            return null;
        }
        return getUser().getUserId();
    }

    public void removeUser() {
        ResponseCookie cookie = ResponseCookie.from(APP_AUTH, "").maxAge(0).path("/").domain(APP_DOMAIN).build();
        ResponseCookie mobileCookie = ResponseCookie.from(APP_AUTH, "").maxAge(0).path("/").domain(APP_DOMAIN).build();
        ResponseCookie userIdCookie = ResponseCookie.from(APP_AUTH, "").maxAge(0).path("/").domain(APP_DOMAIN).build();
        StringJoiner sj = new StringJoiner(";");
        sj.add(cookie.toString()).add(mobileCookie.toString()).add(userIdCookie.toString());
        getResponse().addHeader(HttpHeaders.SET_COOKIE, sj.toString());
        Optional<Cookie> cookieAuth = Arrays.asList(getRequest().getCookies()).stream().filter(p->p.getName().equals(APP_AUTH)).findAny();
        if(cookieAuth.isPresent()){
            loginUser.remove(cookieAuth.get().getValue());
        }
    }

    public static final Integer COOKIE_MAX_AGE = 3600*24*1000;
    public void setUser(String authorization, User user) {
        authorization = authorization.replace(" ", "");
        System.out.printf("set authorization:" + authorization);
        ResponseCookie cookie = ResponseCookie.from(APP_AUTH, authorization)
                .secure(true).domain(APP_DOMAIN).path("/").maxAge(COOKIE_MAX_AGE)
                .sameSite("None").build();
        ResponseCookie mobileCookie = ResponseCookie.from(USER_MOBILE, user.getMobile())
                .secure(true).domain(APP_DOMAIN).path("/").maxAge(COOKIE_MAX_AGE)
                .sameSite("None").build();
        ResponseCookie userIdCookie = ResponseCookie.from(USER_ID, user.getUserId().toString())
                .secure(true).domain(APP_DOMAIN).path("/").maxAge(COOKIE_MAX_AGE)
                .sameSite("None").build();
        StringJoiner sj = new StringJoiner(";");
        sj.add(cookie.toString()).add(mobileCookie.toString()).add(userIdCookie.toString());
        getResponse().addHeader(HttpHeaders.SET_COOKIE, sj.toString());
//        getResponse().addCookie(mobileCookie);
//        getResponse().addCookie(userIdCookie);
//        getResponse().addHeader("Set-cookie", "authorization=" + authorization + ";domain=vr2shipping.com;path=/");
        setLoginUser(authorization, user);
    }

    public User refreshLoginUser(String authorization){
        User user = getLoginUser(authorization);
        if(user != null){
            setUser(authorization, user);
        }
        return user;
    }

    private User getLoginUser(String authorization){
        if(StringUtils.isEmpty(authorization)){
            System.out.println("cannot find login user! auth:" + authorization);
            return null;
        }
        User user = loginUser.get(authorization);
        if(user != null){
            System.out.println("auth:" + authorization + "; userId: " + user);
            return user;
        } else {
            System.out.println("cannot find login user! auth:" + authorization);
            return null;
        }
    }
//    multipal termintal login
//    private ConcurrentHashMap<Long, LinkedList<String>> userIdAuth = new ConcurrentHashMap<>();
//    synchronized void setLoginUser(String authorization, User user){
//        LinkedList<String> authList = userIdAuth.getOrDefault(user.getId(), new LinkedList<>());
//        authList.remove(authorization);
//        if(authList.size()>=user.getTerminalLimit()){
//            String oldAuth = authList.poll();
//            loginUser.remove(oldAuth);
//        }
//        authList.offer(authorization);
//        userIdAuth.put(user.getId(), authList);
//        user.setAuth(authorization);
//        loginUser.put(authorization, user);
//    }

    synchronized void setLoginUser(String authorization, User user){
        loginUser.put(authorization, user);
    }

}
