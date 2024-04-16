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
import com.jones.tank.object.dataapi.OperationType;
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

    @ApiModelProperty("sql模板")
    @TableField("sql_template")
    private String sqlTemplate;

    private JSONObject orderFields;

    private String fromClause;
    private Param query;
    private Param param;
    private Param result;
    private Param order;
    private Map<String, ParamField> fieldMap = new HashMap<>();
    private Map<TableInfo, Set<ParamField>> tableFieldMap = new HashMap<>();

    private Set<TableInfo> tableInfos = new HashSet<>();
    public void initSelectField(){}
    public ParamField getRequestField(String filed){
        return fieldMap.get(filed);
    }

    private Map<String, LinkedList<TableInfo>> entityTableMap = new HashMap<>();
    public void generateJoinInfo(Map<String, TableInfo> tableInfoMap){
        // -----join part--------
        // first version only support one entity
//        Set<TableInfo> tableInfos = new HashSet<>();
        Map<TableInfo, Set<ParamField>> tableFieldMap = new HashMap<>();
        if(queryId != null) {
            for (ParamField field : getQuery().getFields()) {
                TableInfo table = field.getAttribute().getTableInfo();
                tableFieldMap.putIfAbsent(table, new HashSet<>());
                tableFieldMap.get(table).add(field);
                getFieldMap().put(field.getName(), field);
            }
            tableInfos.addAll(getQuery().getFields().stream().map(p->p.getAttribute().getTableInfo()).distinct().collect(Collectors.toList()));
        }
        if(resultId != null) {
            for (ParamField field : getResult().getFields()) {
                TableInfo table = field.getAttribute().getTableInfo();
                tableFieldMap.putIfAbsent(table, new HashSet<>());
                tableFieldMap.get(table).add(field);
            }
            tableInfos.addAll(getResult().getFields().stream().map(p->p.getAttribute().getTableInfo()).distinct().collect(Collectors.toList()));
        }
        if(paramId != null){
            for(ParamField field: getParam().getFields()){
                TableInfo table = field.getAttribute().getTableInfo();
                tableFieldMap.putIfAbsent(table, new HashSet<>());
                tableFieldMap.get(table).add(field);
                getFieldMap().put(field.getName(), field);
            }
            tableInfos.addAll(getParam().getFields().stream().map(p->p.getAttribute().getTableInfo()).distinct().collect(Collectors.toList()));
        }
        this.tableFieldMap = tableFieldMap;
        for(TableInfo table : tableInfos){
//            if(TableType.REL.equals(table.getTableType())){
//                relTable.add(table);
//            }
            if(!StringUtils.isEmpty(table.getEntityType())){
                entityTableMap.putIfAbsent(table.getEntityType(), new LinkedList<>());
                if(TableType.ENTITY.equals(table.getTableType())){
                    entityTableMap.get(table.getEntityType()).offerFirst(table);
                } else {
                    entityTableMap.get(table.getEntityType()).offerLast(table);
                }
            }
        }
//        if(DbType.INSERT.equals(getType()) && tableAttributeMap.keySet().stream().filter(p->p.isAttrTable()).collect(Collectors.toList()).size() > 0){
//            for(String entityField: entityTableMap.keySet()){
//                param.getFields().add(ParamField.builder().name(entityField).operationType(OperationType.EQ).build());
//            }
//        }
        if(!DbType.SELECT.equals(getType())) {
            return;
        }
        StringBuilder fromClause = new StringBuilder();
        for (Map.Entry<String, LinkedList<TableInfo>> entry : entityTableMap.entrySet()) {
            String entityTableName = entry.getKey().toLowerCase();
            TableInfo entityTable = tableInfoMap.get(entityTableName);
            String joinField = entityTable.getEntityId();

            fromClause.append(String.format(" `%s` as `%s` ", entityTable.getName(), entityTableName));
            for (TableInfo table : entry.getValue()) {
                if (table.equals(entityTable)) {
                    continue;
                }
                if (table.isAttrTable()) {
                    for (AttributeInfo attribute : tableFieldMap.get(table).stream().map(p->p.getAttribute()).distinct().collect(Collectors.toList())) {
                        fromClause.append(String.format(" LEFT JOIN  %s on %s.`%s`=%s.`%s` and %s.attr_id = %s",
                                attribute.getFromPart(), attribute.getTableSymbol(), joinField, entityTableName, joinField, attribute.getTableSymbol(), attribute.getId()));
                        if(table.isLogicDelete()){
                            fromClause.append(" and %s.deleted=0".format(attribute.getTableSymbol()));
                        }
                    }
                } else {
                    fromClause.append(String.format(" LEFT JOIN  %s %s on %s.`%s`=%s.`%s` ",
                            table.getName(), table.getName(), table.getName(), joinField, entityTableName, joinField));
                    if(table.isLogicDelete()){
                        fromClause.append(" and %s.deleted=0".format(table.getName()));
                    }
                }
            }
        }
        this.fromClause = fromClause.toString();
    }
    public String getSelectTemplate(Map<String, String> requestParam){
        List<String> whereClause = requestParam.keySet().stream().filter(p->getFieldMap().containsKey(p)).map(p-> getFieldMap().get(p).getWhereTemplate()).collect(Collectors.toList());
        List<String> deletedInfo = entityTableMap.values().stream().filter(p->p.size() > 0 && p.getFirst().isEntityTable()).map(p->String.format("`%s`.deleted=0", p.getFirst().getName())).collect(Collectors.toList());
        whereClause.addAll(deletedInfo);
        StringBuilder query = new StringBuilder("SELECT ")
                .append(String.join(",", getResult().getFields().stream().map(p->p.getSelectPart()).collect(Collectors.toList())))
                .append(" FROM ").append(fromClause)
                .append(" WHERE ")
                .append(String.join(" and ", whereClause));
        if(orderId != null){
            query.append(" ORDER BY ").append(String.join(",", getOrder().getFields().stream().map(p->p.getOrderPart()).collect(Collectors.toList())));
        }
        return query.toString();
    }

    public String getInsertTemplate(TableInfo table, Map<String, String> requestParam){
        if(table.isAttrTable()){
            String fields = String.format("`%s`, `attr_id`, `attr_name`, `value`", table.getEntityId());
            List<String> values = new ArrayList<>();
            for (ParamField field : tableFieldMap.get(table)) {
                if (requestParam.containsKey(field.getName()) && !table.getEntityId().equals(field.getName())) {
                    StringJoiner sj = new StringJoiner(",")
                      .add(String.format("#{%s}", table.getEntityId()))
                      .add(field.getAttribute().getId().toString())
                      .add(field.getAttribute().getName())
                      .add(String.format("#{%s}", field.getName()));
                    values.add(String.format("(%s)",sj.toString()));
                }
            }
            return new StringBuilder("INSERT INTO ").append(table.getName()).append("(")
                    .append(fields).append(") VALUES ")
                    .append(String.join(",", values)).append("; select last_insert_id()").toString();
        } else {
            List<String> fields = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (ParamField field : tableFieldMap.get(table)) {
                if (requestParam.containsKey(field.getName())) {
                    fields.add(String.format("`%s`", field.getAttribute().getName()));
                    values.add(String.format("#{%s}", field.getName()));
                }
            }
            return new StringBuilder("INSERT INTO ").append(table.getName()).append("(")
                    .append(String.join(",", fields)).append(") VALUES (")
                    .append(String.join(",", values)).append("); select last_insert_id()").toString();
        }

    }
    public String getUpdateTemplate(TableInfo table, Map<String, String> requestParam){
        if(table.isAttrTable()) {
            String entityId = requestParam.get(table.getEntityId());
            if(StringUtils.isEmpty(entityId)){
                return null;
            }
            String insertPart = String.format("(`%s`, `attr_id`, `attr_name`, `value`)", table.getEntityId());
            StringJoiner updateSql = new StringJoiner("; ");
            for (ParamField field : param.getFields()) {
                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
                    String valuesPart = String.format("(%s, %s, '%s', #{%s})", entityId, field.getAttributeId(), field.getAttribute().getName(), field.getName());
                    String setPart = new StringJoiner(",")
                            .add(String.format(" `attr_name`='%s'", field.getAttribute().getName()))
                            .add(String.format(" `value`=#{%s}", field.getName()))
                            .toString();
                    updateSql.add(
                            new StringBuilder("INSERT INTO ").append(table.getName()).append(insertPart)
                                    .append(" VALUES ").append(valuesPart)
                                    .append(" ON DUPLICATE KEY UPDATE ").append(setPart)
                    );
                }
            }
            return updateSql.toString();
        } else {
            List<String> setFields = new ArrayList<>();
            for (ParamField field : param.getFields()) {
                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
                    setFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
                }
            }
            if(setFields.isEmpty()) {
                return null;
            }
            List<String> whereFields = new ArrayList<>();
            for (ParamField field : query.getFields()) {
                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
                    whereFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
                }
            }
            if(whereFields.size() == 0){
                return null;
            }
            return new StringBuilder("UPDATE ").append(table.getName())
                    .append(" SET ").append(String.join(",", setFields))
                    .append(" WHERE ").append(String.join(" and ", whereFields)).toString();
        }
    }
//    public String getUpdateTemplate(TableInfo table, Map<String, String> requestParam){
//        if(table.isAttrTable()) {
//            StringJoiner whereJoiner = new StringJoiner(" and ");
//            for (ParamField field : query.getFields()) {
//                if (requestParam.containsKey(field.getName()) && (field.isInTable(table) || field.getName().equals(table.getEntityId()))) {
//                    whereJoiner.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
//                }
//            }
//            String where = whereJoiner.toString();
//            if(where.isEmpty()){
//                return null;
//            }
//            StringJoiner updateSql = new StringJoiner("; ");
//            for (ParamField field : param.getFields()) {
//                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
//                    String setPart = new StringJoiner(",")
//                            .add(String.format(" `value`=#{%s}", field.getName())).toString();
//                    StringJoiner wherePart = new StringJoiner(" and ")
//                            .add(" `attr_id`=" + field.getAttributeId());
//                    if(!where.isEmpty()){
//                        wherePart.add(where);
//                    }
//                    updateSql.add(
//                            new StringBuilder("UPDATE ").append(table.getName())
//                                    .append(" SET ").append(setPart)
//                                    .append(" WHERE ").append(wherePart).toString()
//                    );
//                }
//            }
//            return updateSql.toString();
//        } else {
//            List<String> setFields = new ArrayList<>();
//            for (ParamField field : param.getFields()) {
//                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
//                    setFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
//                }
//            }
//            if(setFields.isEmpty()) {
//                return null;
//            }
//            List<String> whereFields = new ArrayList<>();
//            for (ParamField field : query.getFields()) {
//                if (requestParam.containsKey(field.getName()) && field.isInTable(table)) {
//                    whereFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
//                }
//            }
//            if(whereFields.size() == 0){
//                return null;
//            }
//            return new StringBuilder("UPDATE ").append(table.getName())
//                    .append(" SET ").append(String.join(",", setFields))
//                    .append(" WHERE ").append(String.join(" and ", whereFields)).toString();
//        }
//    }

    public String getDeleteteTemplate(TableInfo table, Map<String, String> requestParam){
        List<String> whereFields = new ArrayList<>();
        for(ParamField field: query.getFields()){
            if(requestParam.containsKey(field.getName()) && field.isInTable(table)){
                whereFields.add(String.format("`%s`=#{%s}", field.getAttribute().getName(), field.getName()));
            }
        }
        if(whereFields.size() == 0){
            return null;
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
