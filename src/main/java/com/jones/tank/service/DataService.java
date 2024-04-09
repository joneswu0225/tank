package com.jones.tank.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.*;
import com.jones.tank.entity.query.*;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.ErrorCode;
import com.jones.tank.object.dataapi.DbType;
import com.jones.tank.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jones
 * @since 2024-03-12
 */
@Service
public class DataService {

    public static final String REQUEST_PREFIX = "/data";
    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private ParamMapper paramMapper;
    @Autowired
    private TableInfoMapper tableInfoMapper;
    @Autowired
    private SqlMapper sqlMapper;
    @Autowired
    private AttributeInfoMapper attributeInfoMapper;

    private ConcurrentHashMap<String, Interface> interfaceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void refresh(){
        List<Interface> interfaceList = interfaceMapper.findAll(new InterfaceQuery());
        List<Param> paramList = paramMapper.findAll(new ParamQuery());
        List<AttributeInfo> attributeInfoList = attributeInfoMapper.findAll(new AttributeInfoQuery());
        List<TableInfo> tableInfoList = tableInfoMapper.findAll(new TableInfoQuery());

        Map<String, TableInfo> tableInfoMap = new HashMap<>();
        Map<Long, AttributeInfo> attributeInfoMap = new HashMap<>();
        Map<Long, TableInfo> tableInfoIdMap = new HashMap<>();
        Map<Long, Param> paramMap = new HashMap<>();
        for(TableInfo table: tableInfoList){
            tableInfoIdMap.put(table.getId(), table);
            tableInfoMap.put(table.getName(), table);
            table.initKeyInfo();
        }
        for(AttributeInfo attributeInfo: attributeInfoList){
            attributeInfo.setTableInfo(tableInfoIdMap.get(attributeInfo.getTableId()));
            attributeInfoMap.put(attributeInfo.getId(), attributeInfo);
        }
        for(Param param: paramList){
            paramMap.put(param.getId(), param);
            param.getFields().forEach(k->{k.setAttribute(attributeInfoMap.get(k.getAttributeId()));});
            param.setRequiredFields(param.getFields().stream().filter(p->ParamField.REQUIRED.equals(p.getRequired())).map(ParamField::getName).collect(Collectors.toList()));
            param.setFieldMap(param.getFields().stream().collect(Collectors.toMap(k->k.getName(), k->k)));
        }
        ConcurrentHashMap<String, Interface> pathMap = new ConcurrentHashMap<>();
        for(Interface item: interfaceList){
            item.setQuery(paramMap.get(item.getQueryId()));
            item.setParam(paramMap.get(item.getParamId()));
            item.setResult(paramMap.get(item.getResultId()));
            item.setOrder(paramMap.get(item.getOrderId()));
            item.generateJoinInfo(tableInfoMap);
            pathMap.put(getPathMapKey(item.getPath(), item.getRequestMethod()), item);
        }
        interfaceMap = pathMap;
    }

    public static BaseResponse validateRequestParam(Param param, Map<String, String[]> requestParam){
        // validate required param
        if(!requestParam.keySet().containsAll(param.getRequiredFields())){
            return BaseResponse.builder().code(ErrorCode.API_PARAM_INVALID).message("必填字段不能为空").build();
        }
        // validate content
        for(String key: requestParam.keySet()){
            ParamField field = param.getFieldMap().get(key);
            String value = requestParam.get(key).toString();
            if(field.getValidateType() != null && !field.getValidateType().validate(value)){
                return BaseResponse.builder().code(ErrorCode.API_PARAM_INVALID).message("字段 \"" + field.getName() + "\" " + field.getValidateType().getDescription()).build();
            }
            if(field.getLengthMin() != null && value.length() < field.getLengthMin()){
                return BaseResponse.builder().code(ErrorCode.API_PARAM_INVALID).message("字段 \"" + field.getName() + "\" 长度至少为 " + field.getLengthMin() + " 个字符").build();
            }
            if(field.getLengthMax() != null && value.length() > field.getLengthMax()){
                return BaseResponse.builder().code(ErrorCode.API_PARAM_INVALID).message("字段 \"" + field.getName() + "\" 长度至多为 " + field.getLengthMin() + " 个字符").build();
            }
        }
        return BaseResponse.builder().build();
    }

    public List<Map<String, Object>> handleSelect(Interface api, Map<String, String> requestParam){
        return sqlMapper.sqlSelectList(api.getSelectTemplate(requestParam), requestParam);
    }

    public String handleUpdate(Interface api, Map<String, String> requestParam){
        Map<TableInfo, Set<AttributeInfo>> tableAttribute = api.getTableParams();
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            for(TableInfo tableInfo: entityTables.getValue()){
                Integer result = sqlMapper.sqlUpdate(api.getUpdateTemplate(tableInfo, requestParam), requestParam);
                sj.add(result.toString());
            }
        }
        return sj.toString();
    }

    public String handleInsert(Interface api, Map<String, String> requestParam){
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            for(TableInfo tableInfo: entityTables.getValue()){
                Integer result = sqlMapper.sqlInsert(api.getUpdateTemplate(tableInfo, requestParam), requestParam);
                sj.add(result.toString());
            }
        }
        return sj.toString();
    }

    public String handleDelete(Interface api, Map<String, String> requestParam){
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            for(TableInfo tableInfo: entityTables.getValue()){
                Integer result = sqlMapper.sqlInsert(api.getDeleteteTemplate(tableInfo, requestParam), requestParam);
                sj.add(result.toString());
            }
        }
        return sj.toString();
    }

    public BaseResponse handleDbExecute(Interface api,  Map<String, String[]> requestParam){
        Map<String, String> requestParams = new HashMap<>();
        // fill default value
        for(ParamField field: api.getQuery().getFields()) {
            if(!StringUtils.isEmpty(field.getDefaultValue())){
                requestParams.put(field.getName(), field.getDefaultValue());
            }
        }
        // format request params
        for(Map.Entry<String, String[]> param: requestParam.entrySet()){
            requestParams.put(param.getKey(),
                    api.getQueryFieldMap().get(param.getKey()).getOperationType().needCollectionParam() ?
                            String.join("','",  param.getValue()) : param.getValue()[0]
            );
        }
        Object result = null;
        switch (api.getType()){
            case SELECT:
                result = handleSelect(api, requestParams);
                break;
            case INSERT:
                result = handleInsert(api, requestParams);
                break;
            case UPDATE:
                result = handleInsert(api, requestParams);
                break;
            case DELETE:
                result = handleDelete(api, requestParams);
                break;
            default:
                break;
        }
        return BaseResponse.builder().data(result).build();
    }

    private static String getPathMapKey(String path, RequestMethod method){
        return method.name().toLowerCase() + path;
    }

    public BaseResponse handleRequest(String path, Map<String, String[]> requestParam, RequestMethod method){
        path = path.substring(REQUEST_PREFIX.length());
        // validate request path
        if(!interfaceMap.containsKey(path)){
            BaseResponse.builder().code(ErrorCode.API_PATH_INVALID).build();
        }
        Interface api = interfaceMap.get(getPathMapKey(path, method));
        // validate request method
        if(!method.equals(api.getRequestMethod())){
            BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
        }
        // validate entity count
        if(!method.equals(api.getRequestMethod())){
            BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
        }
        // upsert接口的实体个数不能超过一个
        if(DbType.INSERT.equals(api.getType()) && (api.getEntityTableMap().keySet().size() > 1)){
            return BaseResponse.builder().code(ErrorCode.API_UPSERT_DUPLICATE_ENTITY).build();
        }
        // validate request param
        BaseResponse resp = validateRequestParam(api.getQuery(), requestParam);
        if(!resp.isSuceeded()){
            return resp;
        }
        // construct request param to db execute
        return handleDbExecute(api, requestParam);
    }

    public BaseResponse post(String path, Map<String, String[]> param){
        return handleRequest(path, param, RequestMethod.POST);
    }

    public BaseResponse get(String path, Map<String, String[]> param){
        return handleRequest(path, param, RequestMethod.GET);
    }


}
