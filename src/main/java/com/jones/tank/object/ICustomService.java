package com.jones.tank.object;

import com.jones.tank.entity.query.Query;

public interface ICustomService<T> {
    Page<T> findPage(Query query);
    BaseResponse findList(Query query);
    BaseResponse add(BaseObject param);
    BaseResponse update(BaseObject param);
    BaseResponse findById(Long id);
    BaseResponse findOne(Query query);
    BaseResponse findByPage(Query query);
    BaseResponse findAll(Query query);
    BaseResponse delete(Long id);

}
