package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(value="系統常量查询参数")
public class ParamQuery extends Query {
    @ApiModelProperty(value="name",name="name")
    private String name;
    @ApiModelProperty(value="参数PARAM/结果RESULT",name="type")
    private String type;
}
