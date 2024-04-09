package com.jones.tank.entity;

import com.alibaba.fastjson.JSONObject;
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

    @ApiModelProperty("参数id，查询参数")
    @TableField("query_id")
    private Long queryId;

    @ApiModelProperty("参数id，请求参数")
    @TableField("param_id")
    private Long paramId;

    @ApiModelProperty("参数id，返回对象")
    @TableField("result_id")
    private Long resultId;

    @ApiModelProperty("参数id，排序参数")
    @TableField("order_id")
    private Long orderId;

    private JSONObject orderFields;

    private String fromClause;
    private Param query;
    private Param param;
    private Param result;
    private Param order;

    private Map<TableInfo, Set<AttributeInfo>> tableParams = new HashMap<>();
    private Map<String, ParamField> queryFieldMap = new HashMap<>();
    private Set<Long> tableIds = new HashSet<>();
    public void initSelectField(){}
    public ParamField getRequestField(String filed){
        return queryFieldMap.get(filed);
    }

    private Map<String, LinkedList<TableInfo>> entityTableMap = new HashMap<>();
    public void generateJoinInfo(Map<String, TableInfo> tableInfoMap){
        if(!DbType.SELECT.equals(getType())){
            return;
        }
        // -----join part--------
        // first version only support one entity
        List<TableInfo> relTable = new ArrayList<>();
        Set<TableInfo> tableInfos = new HashSet<>();
        Map<TableInfo, Set<AttributeInfo>> tableAttributeMap = new HashMap<>();
        for(ParamField field: getQuery().getFields()){
            TableInfo table = field.getAttribute().getTableInfo();
            tableAttributeMap.putIfAbsent(table, new HashSet<>());
            tableAttributeMap.get(table).add(field.getAttribute());
            getQueryFieldMap().put(field.getName(), field);
        }
        for(ParamField field: getResult().getFields()){
            TableInfo table = field.getAttribute().getTableInfo();
            tableAttributeMap.putIfAbsent(table, new HashSet<>());
            tableAttributeMap.get(table).add(field.getAttribute());
        }
        List<TableInfo> tableInfoQuery = getQuery().getFields().stream().map(p->p.getAttribute().getTableInfo()).distinct().collect(Collectors.toList());
        List<TableInfo> tableInfoResult = getResult().getFields().stream().map(p->p.getAttribute().getTableInfo()).distinct().collect(Collectors.toList());

        tableInfos.addAll(tableInfoQuery);
        tableInfos.addAll(tableInfoResult);
        for(TableInfo table : tableInfos){
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
                    for(AttributeInfo attribute : tableAttributeMap.get(table)){
                        fromClause.append(String.format(" LEFT JOIN  %s on %s.`%s`=%s.`id` and %s.attr_id = %s",
                                attribute.getFromPart(), attribute.getTableSymbol(), joinField, entityTableName, attribute.getTableSymbol(), attribute.getId()));
                    }

                }
            }
        }
        this.fromClause = fromClause.toString();
    }
    public String getSelectTemplate(Map<String, String> requestParam){
        StringBuilder query = new StringBuilder("SELECT ")
                .append(String.join(",", getResult().getFields().stream().map(p->p.getSelectPart()).collect(Collectors.toList())))
                .append(" FROM ").append(fromClause)
                .append(" WHERE ")
                .append(String.join(" and ", requestParam.keySet().stream().map(p-> getQueryFieldMap().get(p).getWhereTemplate()).collect(Collectors.toList())));
        if(orderId != null){
            query.append(String.join(",", getOrder().getFields().stream().map(p->p.getOrderPart()).collect(Collectors.toList())));
        }
        return query.toString();
    }

    public String getInsertTemplate(TableInfo table, Map<String, String> requestParam){
        List<String> fields = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for(ParamField field: param.getFields()){
            if(requestParam.containsKey(field.getName())){
                fields.add(String.format("`%s`", field.getAttribute().getName()));
                values.add(String.format("#{%s}", field.getName()));
            }
        }
        return new StringBuilder("INSERT INTO ").append(table.getName()).append("(")
                .append(String.join(",", fields)).append(") VALUES (")
                .append(String.join(",", values)).append(")").toString();
    }

    public String getUpdateTemplate(TableInfo table, Map<String, String> requestParam){
        List<String> setFields = new ArrayList<>();
        List<String> whereFields = new ArrayList<>();
        for(ParamField field: param.getFields()){
            if(requestParam.containsKey(field.getName())){
                setFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
            }
        }
        for(ParamField field: query.getFields()){
            if(requestParam.containsKey(field.getName())){
                whereFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
            }
        }
        return new StringBuilder("UPDATE ").append(table.getName())
                .append(" SET ").append(String.join(",", setFields))
                .append(" WHERE ").append(String.join(" and ", whereFields)).toString();
    }
    public String getDeleteteTemplate(TableInfo table, Map<String, String> requestParam){
        List<String> setFields = new ArrayList<>();
        List<String> whereFields = new ArrayList<>();
        for(ParamField field: param.getFields()){
            if(requestParam.containsKey(field.getName())){
                setFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
            }
        }
        for(ParamField field: query.getFields()){
            if(requestParam.containsKey(field.getName())){
                whereFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
            }
        }
        if(table.isLogicDelete()){
            return new StringBuilder("UPDATE ").append(table.getName())
                    .append(" SET deleted=1 ")
                    .append(" WHERE ").append(String.join(" and ", whereFields)).toString();
        } else {
            return new StringBuilder("DELETE FROM ").append(table.getName())
                .append(" WHERE ").append(String.join(" and ", whereFields)).toString();
        }

    }



}
