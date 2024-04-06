package com.jones.tank.service;

import com.jones.tank.entity.Interface;
import com.jones.tank.object.BaseObject;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.CustomServiceImpl;
import com.jones.tank.repository.InterfaceMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jones
 * @since 2024-03-12
 */
@Service
public class InterfaceService extends CustomServiceImpl<InterfaceMapper, Interface> {

    public BaseResponse handleInterface(String id, HttpServletRequest request, HttpServletResponse response) {
        System.out.printf(request.getRequestURI());
        return BaseResponse.builder().build();
    }
}
