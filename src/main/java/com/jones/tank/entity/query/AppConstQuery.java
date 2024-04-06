package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel(value="系統常量查询参数")
@Builder
public class AppConstQuery extends Query {
    @ApiModelProperty(value="常量名",name="name")
    private String name;
    @ApiModelProperty(value="常量名",name="nameLike")
    private String nameLike;
}
