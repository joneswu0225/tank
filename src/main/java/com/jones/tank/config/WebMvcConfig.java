package com.jones.tank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Autowired
    private TotalInterceptor totalInterceptor;
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    protected CustomRequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        CustomRequestMappingHandlerMapping customRequestMappingHandlerMapping = new CustomRequestMappingHandlerMapping();
        customRequestMappingHandlerMapping.initHandleInterface(getApplicationContext());
        return customRequestMappingHandlerMapping;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(totalInterceptor).addPathPatterns("/**");
        registry.addInterceptor(authInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/error")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/wxlogin")
                .excludePathPatterns("/user/regist")
                .excludePathPatterns("/data/const")
        ;
    }



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

