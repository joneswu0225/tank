package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
@NoArgsConstructor
@Accessors(chain = true)
@TableName("param")
@ApiModel(value = "param对象", description = "")
public class Param implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("id")
    private Long id;

    @TableField("name")
    private String name;

    @ApiModelProperty("参数PARAM/结果RESULT")
    @TableField("type")
    private String type;

    @TableField("detail")
    private String detail;

    private List<String> requiredFields = new ArrayList<>();
    private List<ParamField> fields = new ArrayList<>();
    private Map<String, ParamField> fieldMap;

}
