package com.jones.tank.config;

import com.jones.tank.object.ApplicationConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Configuration
public class TotalInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String appSource = request.getHeader(ApplicationConst.APP_SOURCE_FIELD);
        log.info("appSource: " + appSource);
        if("/user/login".equals(request.getRequestURI())){
            request.setAttribute("appSource", appSource);
        }
        return true;
    }
}
