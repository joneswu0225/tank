package com.jones.tank.config;

import com.jones.tank.controller.DataController;
import com.jones.tank.service.DataService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Configuration
public class InterfaceInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if(!uri.startsWith(DataController.REQUEST_PREFIX)){
            return true;
        }
        request.setAttribute("a", "bb");
        request.getParameterMap();
//        this.requestInterceptor.preHandle(new DispatcherServletWebRequest(request, response));
        String method = request.getMethod();
        System.out.println("preHandle" + request.getRequestURI());
        return true;
    }

}
