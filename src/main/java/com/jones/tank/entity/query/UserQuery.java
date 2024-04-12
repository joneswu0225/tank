package com.jones.tank.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="用户查询参数")
@Builder
public class UserQuery extends Query {
    @ApiModelProperty(value="姓名",name="sgname")
    private String sgname;
    @ApiModelProperty(value="昵称",name="nickname")
    private String nickname;
    @ApiModelProperty(value="手机号",name="mobile")
    private String mobile;
    @ApiModelProperty(value="密码",name="password")
    private String password;
    @ApiModelProperty(value="验证码",name="verifyCode")
    private String verifyCode;
    @ApiModelProperty(value="用户类型",name="userType")
    private Integer userType;
    @ApiParam(hidden = true)
    private String openid;
}

