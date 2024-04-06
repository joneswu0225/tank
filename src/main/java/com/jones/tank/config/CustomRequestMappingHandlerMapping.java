package com.jones.tank.config;

import com.jones.tank.controller.DataController;
import com.jones.tank.controller.InterfaceController;
import com.jones.tank.service.DataService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

//@Component
//public class CustomRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
public class CustomRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    private static final String URL_PREFIX_INTERFACE = "/data";

    private ConcurrentHashMap<String, HandlerMethod> proxyMethod = new ConcurrentHashMap<>();
    public CustomRequestMappingHandlerMapping(){

    }

//    @PostConstruct
    public void initHandleInterface(ApplicationContext context){
        String methodName = "handleInterface";
        try {
//            ApplicationContext context = getApplicationContext();
            DataController handlerBean = context.getBean(DataController.class);
            for(Method method : handlerBean.getClass().getDeclaredMethods()){
                proxyMethod.put(method.getName().toUpperCase(), new HandlerMethod(handlerBean, method));
            }
        } catch(Exception e){
            logger.error("cannot find resolve method" + methodName);
        }
    }
    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
//        if(this.interfaceHandlerMethod == null){
//            this.initHandleInterface();
//        }
        if(lookupPath.startsWith(DataService.REQUEST_PREFIX)){
            return this.proxyMethod.get(request.getMethod());
        }
        return super.lookupHandlerMethod(lookupPath, request);
    }



}
