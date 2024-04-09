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
    @Autowired
    private DataService service;


    @RequestMapping(value="get", method={RequestMethod.GET})
    public BaseResponse get(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.get(request.getRequestURI(), request.getParameterMap());
    }

    @RequestMapping(value="post", method={RequestMethod.POST})
    public BaseResponse post(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
        return service.post(request.getRequestURI(), request.getParameterMap());
    }

//    @RequestMapping(value="patch", method={RequestMethod.PATCH})
//    public BaseResponse patch(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
//        return post(params, request, response);
//    }

//    @RequestMapping(value="put", method={RequestMethod.PUT})
//    public BaseResponse put(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
//        return post(params, request, response);
//    }

    @RequestMapping(value="delete", method={RequestMethod.DELETE})
    public BaseResponse delete(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception{
        return post(params, request, response);
    }

}
