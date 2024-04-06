package com.jones.tank.controller;

import com.jones.tank.entity.param.BaseParam;
import com.jones.tank.entity.query.Query;
import com.jones.tank.object.BaseObject;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.CustomServiceImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Controller
abstract class BaseController<M extends CustomServiceImpl> {
    protected M service;

    protected int size = 20;
    protected int page = 1;
    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Autowired
    private ApplicationContext applicationContext;
    @PostConstruct
    void init(){
        this.service = getService();
    }

    @ApiOperation(value = "根据ID查询一条记录", notes = "根据ID查询一条记录")
    @RequestMapping(value="{id}", method={RequestMethod.GET})
    public BaseResponse findById(@PathVariable Long id, HttpServletRequest request) throws Exception{
        service.findById(id);
        return BaseResponse.builder().data(id).build();
    }

    @ApiOperation(value = "根据ID更新一条记录", notes = "根据ID更新一条记录")
    @PutMapping("{id}")
    public BaseResponse update(@PathVariable Long id, @RequestParam BaseObject param) {
        param.setId(id);
        return service.update(param);
    }

    @ApiOperation(value = "根据ID删除一条记录", notes = "根据ID删除一条记录")
    @DeleteMapping("{id}")
    public BaseResponse delete(@PathVariable Long id) {
        return service.delete(id);
    }



    @ApiOperation(value = "分页列表", notes = "分页列表")
    @GetMapping("")
    public BaseResponse listPage(@ApiParam Query query){
        return service.findByPage(query);
    }


    @ApiOperation(value = "分页列表", notes = "分页列表")
    @GetMapping("/all")
    public BaseResponse all(@ApiParam Query query){
        return service.findAll(query);
    }

    abstract M getService();
}
