package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author jones
 * @since 2024-03-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user")
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("mobile")
    private String mobile;

    @TableField("password")
    private String password;

    @ApiModelProperty("账户状态，1：启用，2：冻结")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty("最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("用户类型，0：普通注册用户，1：企业管理员，2：系统管理员")
    @TableField("user_type")
    private Integer userType;

    @ApiModelProperty("最近10次登录ip，逗号分隔")
    @TableField("ip")
    private String ip;

    @ApiModelProperty("手机验证码")
    @TableField("verify_code")
    private String verifyCode;

    @ApiModelProperty("唯一识别码")
    @TableField("unique_code")
    private String uniqueCode;

    @TableField("delete_flg")
    private Boolean deleteFlg;

    @TableField("openid")
    private String openid;

    @TableField("unionid")
    private String unionid;


}
