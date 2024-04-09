package com.jones.tank.entity.param;

import com.jones.tank.util.support.ValidMobile;
import com.jones.tank.util.support.ValidPassword;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value="重置密码参数")
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordRestParam {
    @ValidMobile
    @ApiModelProperty(value="手机号",name="mobile")
    private String mobile;
    @ValidPassword
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value="密码",name="password")
    private String password;
//    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty(value="验证码",name="verifyCode")
    private String verifyCode;
    @ApiModelProperty(value="旧密码",name="passwordOld")
    private String passwordOld;
}

