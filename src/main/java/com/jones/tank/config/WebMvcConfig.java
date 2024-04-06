package com.jones.tank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
//    @Autowired
//    private InterfaceInterceptor interfaceInterceptor;
//    @Qualifier("customRequestMappingHandlerMapping")

    @Override
    protected CustomRequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        CustomRequestMappingHandlerMapping customRequestMappingHandlerMapping = new CustomRequestMappingHandlerMapping();
        customRequestMappingHandlerMapping.initHandleInterface(getApplicationContext());
        return customRequestMappingHandlerMapping;
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(interfaceInterceptor); //.addPathPatterns("/interface/req");
//    }



    /**
     * 功能描述: 处理swagger不展示的问题
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }

}

