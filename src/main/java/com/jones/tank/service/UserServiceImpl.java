package com.jones.tank.service;

import com.jones.tank.entity.User;
import com.jones.tank.object.CustomServiceImpl;
import com.jones.tank.repository.UserMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jones
 * @since 2024-03-12
 */
@Service
public class UserServiceImpl extends CustomServiceImpl<UserMapper, User> {

}
