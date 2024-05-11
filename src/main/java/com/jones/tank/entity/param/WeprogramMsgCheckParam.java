package com.jones.tank.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@ApiModel(value="weprogram_msg_check", description = "weprogram_msg_check")
@NoArgsConstructor
@AllArgsConstructor
public class WeprogramMsgCheckParam implements Serializable {
    @ApiParam(hidden = true)
    private Integer scene = 1;
    @ApiParam(hidden = true)
    private Integer version = 2;
    @ApiModelProperty(value="openid",name="opendi")
    private String openid;
    @ApiModelProperty(value="content",name="content")
    private String content;
}