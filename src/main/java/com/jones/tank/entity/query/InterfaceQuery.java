package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(value="系統常量查询参数")
public class InterfaceQuery extends Query {
    @ApiModelProperty(value="接口路径",name="path")
    private String path;
    @ApiModelProperty(value="接口类型QUERY/UPDATE/INSERT/DELETE",name="type")
    private String type;
    @ApiModelProperty(value="请求类型GET/POST",name="requestMethod")
    private String requestMethod;
    @ApiModelProperty(value="实体ID",name="entityId")
    private String entityId;
    @ApiModelProperty(value="参数id，请求参数",name="paramId")
    private String paramId;
    @ApiModelProperty(value="参数id，返回对象",name="resultId")
    private String resultId;
}
