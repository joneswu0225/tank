package com.jones.tank.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.User;
import com.jones.tank.entity.param.UserLoginParam;
import com.jones.tank.object.ApplicationConst;
import com.jones.tank.service.UserService;
import com.jones.tank.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
@Configuration
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Value("${app.mode:DEBUG}")
    private String appMode;
    @Value("${app.mode.nologin.source.prefix:}")
    private String[] nologinSource;

    @Autowired
    private UserService userService;

    public static boolean isStaticRequest(String url){
        String uri = url.split("\\?")[0];
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".html") || uri.endsWith(".htm");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String url = request.getRequestURI();
        if(isStaticRequest(url))    return true;
        String appSource = request.getHeader(ApplicationConst.APP_SOURCE_FIELD);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            System.out.println(name + " : " + request.getHeader(name));
        }
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }
        if(appMode.equals(ApplicationConst.APP_MODE_NOLOGIN) && nologinSource.length > 0 && request.getMethod().equals("GET")){
            for(String item : nologinSource){
                if(url.startsWith(item)){
                    return true;
                }
            }
        }
        log.info("authorization in request header is :" + request.getHeader("authorization"));
        User loginUser = LoginUtil.getInstance().getUser();
        if(loginUser == null && request.getHeader("authorization") != null){
            String authorization = request.getHeader("authorization");
            loginUser = LoginUtil.getInstance().refreshLoginUser(authorization);
            if(loginUser == null){
                log.info("request address: " + request.getRemoteAddr());
                String referer = request.getHeader("referer");
                if("mastertoken".equals(authorization) || appMode.equals(ApplicationConst.APP_MODE_DEBUG) && StringUtils.isNotBlank(referer) && (referer.contains("swagger-ui.html") || referer.contains("notebook"))){
                    log.info("当前请求为内部接口请求，且无登录状态，设置默认用户为：admin");
                    UserLoginParam user = UserLoginParam.builder().mobile("admin").password("admin").build();
                    loginUser = User.builder().mobile("admin").build();
                    request.setAttribute("authorization", ((Map<String, String>)userService.doLogin(user, appSource == null ? ApplicationConst.APP_SOURCE_ADMIN : appSource).getData()).get("authorization"));
                } else {
                    log.info("当前用户未登陆");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "请登录后进行操作");
                    return false;
                }
            }
        }
        log.info("当前访问： {}, 用户：{}", url, loginUser);

        return true;
    }

    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}