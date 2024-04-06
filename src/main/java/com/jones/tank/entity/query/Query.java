package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
@ApiModel(value="分页参数")
public class Query {
    private static final long DEFAULT_PAGE_NUM = 1;
    private static final long DEFAULT_PAGE_SIZE = 20;
    @ApiParam(hidden = true)
    private Long id;
    @ApiModelProperty(value="页码",name="page")
    private Long page = DEFAULT_PAGE_NUM;
    @ApiModelProperty(value="每页长度",name="size")
    private Long size = DEFAULT_PAGE_SIZE;
    @ApiParam(hidden = true)
    private Long startRow;
    @ApiParam(hidden = true)
    private Object query;

    public Long getStartRow(){
        Long page = this.page < 1 ? 1 : this.page;
        return Long.valueOf((page - 1) * size);
    }

    public Query(){}

    public Query(Object query){
        this.query = query;
    }
}
