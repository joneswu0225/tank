package com.jones.tank.controller;


import com.jones.tank.config.ProxyInterface;
import com.jones.tank.entity.query.Query;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.service.DataService;
import com.jones.tank.service.InterfaceService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/interface")
@Api(value = "系統常量", tags = {"系統常量"})
public class InterfaceController extends BaseController<InterfaceService>{
    @Autowired
    private InterfaceService service;
    @Autowired
    private DataService dataService;

    @Override
    InterfaceService getService() {
        return this.service;
    }


    @RequestMapping(value="refresh", method={RequestMethod.GET})
    public BaseResponse refresh(){
        dataService.refresh();
        return BaseResponse.builder().build();
    }

    @RequestMapping(value="/a/{id}", method={RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.POST})
    public BaseResponse fileDownLoad2(@PathVariable String id, HttpServletRequest request) throws Exception{
//        if(request.getMethod().equals(RequestMethod.GET.name())){
//            return fileDownLoad1(id);
//        }
        System.out.println(id);
        return BaseResponse.builder().data(id).build();
    }



}
