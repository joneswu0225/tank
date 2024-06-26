package com.jones.tank.controller;


import com.jones.tank.entity.query.Query;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.service.DataService;
import com.jones.tank.service.InterfaceService;
import io.swagger.annotations.Api;
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
@RequestMapping("/data")
//@Api(value = "代理接口", tags = {"代理接口"})
public class DataController {
    public static final String REQUEST_PREFIX = "/data";

    @Autowired
    private DataService service;

    private String getUrl(HttpServletRequest request){
        return request.getRequestURI().replaceAll("^/+", "/").substring(REQUEST_PREFIX.length());
    }

    @RequestMapping(value="get", method={RequestMethod.GET})
    public BaseResponse get(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.get(getUrl(request), request.getParameterMap());
    }

    @RequestMapping(value="post", method={RequestMethod.POST})
    public BaseResponse post(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.post(getUrl(request), request.getParameterMap(), params);
    }

    @RequestMapping(value="put", method={RequestMethod.PUT})
    public BaseResponse put(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.put(getUrl(request), request.getParameterMap(), params);
    }

    @RequestMapping(value="delete", method={RequestMethod.DELETE})
    public BaseResponse delete(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.delete(getUrl(request), request.getParameterMap());
    }

}
