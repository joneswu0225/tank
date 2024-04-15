package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.jones.tank.object.dataapi.TableType;
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
@TableName("attribute_info")
@ApiModel(value = "AttributeInfo对象", description = "")
public class AttributeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("entity_type")
    private String entityType;

    @TableField("detail")
    private String detail;

    @TableField("table_id")
    private Long tableId;

    @TableField("table_name")
    private String tableName;

    @ApiModelProperty("数据类型(STRING/NUMBER/CALENDAR)")
    @TableField("content_type")
    private String contentType;

    @ApiModelProperty("是否加入标签计算")
    @TableField("tag_flg")
    private Integer tagFlg;

    private TableInfo tableInfo;

    public String getTableSymbol(){
        return getTableInfo().isAttrTable() ? String.format("%s_%s", getTableInfo().getName(), getId()) : getTableInfo().getName();
    }

    public String getAttributeName(){
        return getTableInfo().isAttrTable() ? "value" : getName();
    }

    public String getSqlFieldName(){
        String tableSymbol = getTableName();
        String attributeName = getName();
        if(getTableInfo().isAttrTable()){
            tableSymbol = String.format("%s_%d", getTableInfo().getName(), getId());
            attributeName = "value";
        }
        return String.format("`%s`.`%s`", tableSymbol, attributeName);
    }


    public String getUpdatePart(String value){
        return String.format("%s='%s'".format(getName(), value));
    }

    public String getSelectPart() {
        return String.format("%s as `%s`", getSqlFieldName(), name);
    }

    public String getFromPart(){
        return String.format("`%s` as `%s`", getTableInfo().getName(), getTableSymbol());
    }

}
