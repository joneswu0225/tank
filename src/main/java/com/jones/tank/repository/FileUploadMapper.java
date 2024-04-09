package com.jones.tank.repository;

import com.jones.tank.entity.FileUpload;
import com.jones.tank.entity.query.Query;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileUploadMapper extends CommonMapper<FileUpload> {
    List<Object> findAllName(Query query);
}
