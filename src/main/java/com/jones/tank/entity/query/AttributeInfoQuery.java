package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(value="系統常量查询参数")
public class AttributeInfoQuery extends Query {
    @ApiModelProperty(value="name",name="name")
    private String name;
    @ApiModelProperty(value="业务类型",name="entityType")
    private String entityType;
    @ApiModelProperty(value="数据类型(STRING/NUMBER/CALENDAR)",name="contentType")
    private String contentType;
    @ApiModelProperty(value="tableId",name="tableId")
    private String tableId;
}
