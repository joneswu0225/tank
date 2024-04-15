package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.jones.tank.object.dataapi.ContentType;
import com.jones.tank.object.dataapi.OperationType;
import com.jones.tank.object.dataapi.TableType;
import com.jones.tank.object.dataapi.ValidateType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
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
//@Builder
@Accessors(chain = true)
@TableName("param_field")
@ApiModel(value = "ParamField对象", description = "")
public class ParamField implements Serializable {
    public static final Integer REQUIRED = 1;
    public static final Integer NOT_REQUIRED = 0;
    public static final String ORDER_BY_ASC = "0";
    public static final String ORDER_BY_DESC = "1";
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("detail")
    private String detail;

    @TableField("param_id")
    private Long paramId;

    @TableField("attribute_id")
    private Long attributeId;

    @ApiModelProperty("是否必须")
    @TableField("required")
    private Integer required;

    @ApiModelProperty("默认值")
    @TableField("default_value")
    private String defaultValue;

    @ApiModelProperty("数据类型(STRING/NUMBER/CALENDAR)")
    @TableField("content_type")
    private ContentType contentType;

    @ApiModelProperty("字段校验方式(EMAIL/MOBILE/PLAIN)")
    @TableField("validate_type")
    private ValidateType validateType;

    @ApiModelProperty("比较方式(EQ,GT,LT,GTE,LTE,IN,NIN,LK)")
    @TableField("operationType")
    private OperationType operationType;

    @ApiModelProperty("校验字段，字段最短长度")
    @TableField("length_min")
    private Integer lengthMin;

    @ApiModelProperty("校验字段，字段最大长度")
    @TableField("length_max")
    private Integer lengthMax;

    @ApiModelProperty("排序")
    @TableField("seq")
    private Integer seq;

    private AttributeInfo attribute;

//    public String getTableSymbol(){
//        return attribute.getTableSymbol();
//    }
//
//    private String getSqlFieldName(){
//        return attribute.getSqlFieldName();
//    }
//


    public String getWhereTemplate(){
        if(this.getOperationType().needCollectionParam()){
            return getAttribute().getSqlFieldName() + String.format(getOperationType().symbol, "${" + getName() + "}");
        } else {
            return getAttribute().getSqlFieldName() + String.format(getOperationType().symbol, "#{" + getName() + "}");
        }
    }
//
//    public String getUpdatePart(String value){
//        return attribute.getUpdatePart(value);
//    }
//
    public String getSelectPart() {
        return attribute.getSelectPart();
    }


    public String getOrderPart(){
        return String.format("%s %s", attribute.getSqlFieldName(), ORDER_BY_DESC.equals(defaultValue) ? "DESC": "");
    }

    public boolean isInTable(TableInfo tableInfo){
        return getAttribute().getTableInfo().equals(tableInfo);
    }
//
//    public String getFromPart(){
//        return attribute.getFromPart();
//    }

}
