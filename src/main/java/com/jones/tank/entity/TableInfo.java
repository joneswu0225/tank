package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
@TableName("table_info")
@ApiModel(value = "TableInfo对象", description = "")
public class TableInfo implements Serializable {
    public static final String TABLE_TYPE_ENTITY = "ENTITY";
    public static final String TABLE_TYPE_ATTR = "ATTR";
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("detail")
    private String detail;

    @ApiModelProperty("实体类型名称")
    @TableField("entity_type")
    private String entityType;

    @ApiModelProperty("实体(ENTITY)/属性(ATTR)/关系(REL)/常量(CONST)/指标(INDIC)")
    @TableField("table_type")
    private TableType tableType;

    @TableField("key1")
    private String key1;

    @TableField("key2")
    private String key2;

    @TableField("key3")
    private String key3;

    @TableField("rel1")
    private String rel1;

    @TableField("rel2")
    private String rel2;

    @TableField("rel3")
    private String rel3;

    private Set<String> keys = new HashSet<>();

    public boolean isAttrTable(){
        return TableType.ATTR.equals(this.tableType);
    }

}
