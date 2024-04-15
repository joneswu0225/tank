package com.jones.tank.repository;


import com.jones.tank.entity.query.Query;

import java.util.List;

public interface CommonMapper<T> {
    List<T> findList(Query paramQuery);

    default T findOne(Query paramQuery){
        List<T> dataList = this.findList(paramQuery);
        return dataList.size()>0 ? dataList.get(0) : null;
    }

    T findById(Long id);

    Long findCount(Query paramQuery);

    List<T> findAll(Query paramQuery);

    void insert(Object param);

    default void insertMany(Object param){
        this.insert(param);
    }

    void update(Object param);

    void delete(Object param);
}

