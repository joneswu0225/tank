package com.jones.tank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.object.dataapi.DbType;
import com.jones.tank.object.dataapi.TableType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javafx.scene.control.Tab;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.RequestMethod;

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
@TableName("interface")
@ApiModel(value = "Interface对象", description = "")
public class Interface implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("接口路径")
    @TableField("path")
    private String path;

    @TableField("detail")
    private String detail;

    @ApiModelProperty("后期用于权限控制")
    @TableField("entity_id")
    private Long entityId;

    @ApiModelProperty("接口类型QUERY/UPDATE/INSERT/DELETE")
    @TableField("type")
    private DbType type;

    @ApiModelProperty("请求类型GET/POST")
    @TableField("request_method")
    private RequestMethod requestMethod;

    @ApiModelProperty("参数id，请求参数")
    @TableField("param_id")
    private Long paramId;

    @ApiModelProperty("参数id，返回对象")
    @TableField("result_id")
    private Long resultId;

    private String fromClause;
    private Param param;
    private Param result;

    private List<String> query;
    private Map<TableInfo, Set<AttributeInfo>> tableParams = new HashMap<>();
    private Map<String, ParamField> paramFieldMap = new HashMap<>();

    public void initSelectField(){}
    public ParamField getRequestField(String filed){
        return paramFieldMap.get(filed);
    }

    public void generateJoinInfo(Map<String, TableInfo> tableInfoMap){
        // -----join part--------
        // first version only support one entity
        Map<String, LinkedList<TableInfo>> entityTableMap = new HashMap<>();
        List<TableInfo> relTable = new ArrayList<>();
        for(TableInfo table : getTableParams().keySet()){
            if(TableType.REL.equals(table.getTableType())){
                relTable.add(table);
            }
            if(!StringUtils.isEmpty(table.getEntityType())){
                entityTableMap.putIfAbsent(table.getEntityType(), new LinkedList<>());
                if(TableType.ENTITY.equals(table.getTableType())){
                    entityTableMap.get(table.getEntityType()).offerFirst(table);
                } else {
                    entityTableMap.get(table.getEntityType()).offerLast(table);
                }
            }
        }
        StringBuilder fromClause = new StringBuilder();
        for(Map.Entry<String, LinkedList<TableInfo>> entry: entityTableMap.entrySet()){
            String entityTableName = entry.getKey().toLowerCase();
            String joinField = entityTableName + "_id";
            TableInfo entityTable = tableInfoMap.get(entityTableName);
            fromClause.append(String.format(" %s %s ", entityTable.getName(), entityTableName));
            for(TableInfo table: entry.getValue()){
                if(table.equals(entityTable)){
                    continue;
                }
                if(TableType.ENTITY.equals(table.getTableType())){
                    fromClause.append(String.format(" LEFT JOIN  %s %s on %s.`%s`=%s.`%s` ",
                            table.getName(), table.getName(), table.getName(), joinField, entityTableName, joinField));
                } else {
                    for(AttributeInfo attribute : getTableParams().get(table)){
                        fromClause.append(String.format(" LEFT JOIN  %s on %s.`%s`=%s.`id` and %s.attr_id = %s",
                                attribute.getFromPart(), attribute.getTableSymbol(), joinField, entityTableName, attribute.getTableSymbol(), attribute.getId()));
                    }

                }
            }
        }
        this.fromClause = fromClause.toString();
    }
    public String getSelectTemplate(Map<String, String> requestParam){
       return new StringBuilder("SELECT ")
                .append(String.join(",", getResult().getFields().stream().map(p->p.getSelectPart()).collect(Collectors.toList())))
                .append(" FROM ").append(fromClause)
                .append(" WHERE ")
                .append(String.join(" and ", requestParam.keySet().stream().map(p->getParamFieldMap().get(p).getWhereTemplate()).collect(Collectors.toList()))).toString();
    }

}
