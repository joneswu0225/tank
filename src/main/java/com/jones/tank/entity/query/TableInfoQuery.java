package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(value="系統常量查询参数")
public class TableInfoQuery extends Query {
    @ApiModelProperty(value="name",name="name")
    private String name;
    @ApiModelProperty(value="实体(ENTITY)/属性(ATTR)/关系(REL)/常量(CONST)/指标(INDIC)",name="tableType")
    private String tableType;
    @ApiModelProperty(value="实体类型名称",name="entityType")
    private String entityType;
}
