package com.jones.tank.controller;


import com.jones.tank.object.BaseResponse;
import com.jones.tank.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jones
 * @since 2024-03-12
 */
@RestController
@RequestMapping("/open")
//@Api(value = "代理接口", tags = {"代理接口"})
public class OpenController {
    public static final String REQUEST_PREFIX = "/open";

    @Autowired
    private DataService service;

    private String getUrl(HttpServletRequest request){
        return request.getRequestURI().replaceAll("^/+", "/").substring(REQUEST_PREFIX.length());
    }

    @RequestMapping(value="get", method={RequestMethod.GET})
    public BaseResponse get(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.get(getUrl(request), request.getParameterMap());
    }

}
