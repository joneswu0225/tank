package com.jones.tank.entity.param;

import com.jones.tank.util.support.ValidMobile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@ApiModel(value="重置密码参数", description = "验证码，密码不能同时为空")
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginParam {
    @ValidMobile
    @ApiModelProperty(value="手机号",name="mobile")
    private String mobile;
    @ApiModelProperty(value="密码",name="password")
    private String password;
    @ApiModelProperty(value="验证码",name="verifyCode")
    private String verifyCode;
    @ApiParam(hidden = true)
    private Integer appSource;
}

