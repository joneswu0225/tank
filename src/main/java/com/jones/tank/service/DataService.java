package com.jones.tank.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.*;
import com.jones.tank.entity.query.*;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.ErrorCode;
import com.jones.tank.object.dataapi.DbType;
import com.jones.tank.repository.*;
import com.jones.tank.util.LoginUtil;
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

//    @Scheduled(fixedRate = 10000)
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

    public static BaseResponse validateRequestParam(Param param, Map requestParam){
        if(param == null || requestParam == null){
            return BaseResponse.builder().build();
        }
        // validate required param
        for (String requiredFiled : param.getRequiredFields()) {
            if (!requestParam.containsKey(requiredFiled) || StringUtils.isEmpty((String) requestParam.get(requiredFiled)) ) {
                return BaseResponse.builder().code(ErrorCode.API_PARAM_INVALID).message(String.format("必填字段 %s 不能为空", requiredFiled)).build();
            }
        }
        // validate content
        for(Object key: requestParam.keySet()){
            ParamField field = param.getFieldMap().get(key);
            if(field == null){
                continue;
            }
            String value = null;
            if(requestParam.get(key) instanceof String[]){
                value = String.join(",", (String[])requestParam.get(key));
            }  else {
                value = (String) requestParam.get(key);
            }
            if(value == null){
                continue;
            }
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
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            for(TableInfo tableInfo: entityTables.getValue()){
                String sqlTemplate = api.getUpdateTemplate(tableInfo, requestParam);
                if(sqlTemplate != null) {
                    Integer result = sqlMapper.sqlUpdate(sqlTemplate, requestParam);
                    sj.add(result.toString());
                }
            }
        }
        String result = sj.toString();
        return StringUtils.isEmpty(result) ? null : result;
    }

    public String handleInsert(Interface api, Map<String, String> requestParam){
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            TableInfo entityTable = entityTables.getValue().getFirst();
            Long entityId = null;
            for(TableInfo tableInfo: entityTables.getValue()){
                if(tableInfo.equals(entityTable)){
                    entityId = sqlMapper.sqlInsert(api.getInsertTemplate(tableInfo, requestParam), requestParam);
                    requestParam.put(tableInfo.getEntityId(), entityId.toString());
                    sj.add(entityId.toString());
                } else if(entityId != null){
                    Long result = sqlMapper.sqlInsert(api.getInsertTemplate(tableInfo, requestParam), requestParam);
                    sj.add(result.toString());
                }
            }
        }
        return sj.toString();
    }

    public String handleDelete(Interface api, Map<String, String> requestParam){
        StringJoiner sj = new StringJoiner(",");
        for(Map.Entry<String, LinkedList<TableInfo>> entityTables: api.getEntityTableMap().entrySet()){
            for(TableInfo tableInfo: entityTables.getValue()){
                String sqlTemplate = api.getDeleteteTemplate(tableInfo, requestParam);
                if(sqlTemplate != null){
                    Integer result = sqlMapper.sqlUpdate(sqlTemplate, requestParam);
                    sj.add(result.toString());
                }
            }
        }
        String result = sj.toString();
        return StringUtils.isEmpty(result) ? null : result;
    }

    private static final List<String> INNER_PARAM = Arrays.asList("page_size", "page_number", "login_user_id");

    private static Map<String, String> prepareParams(Interface api,  Map<String, String[]> queryParam, Map<String, String> requestParam){
        Map<String, String> requestParams = new HashMap<>();
        // format request params
        if(queryParam != null) {
            for (Map.Entry<String, String[]> param : queryParam.entrySet()) {
                String key = param.getKey();
                String value = param.getValue()[0];
                if(INNER_PARAM.contains(key)){
                    requestParams.put(key, value);
                } else if(api.getFieldMap().containsKey(key)){
                    requestParams.put(param.getKey(),
                            api.getFieldMap().get(param.getKey()).getOperationType().needCollectionParam() ?
                                    String.format("\"%s\"", String.join("\",\"", value.split(","))) : value
                    );
                }
            }
        }
        if(requestParam!=null) {
            for (Map.Entry<String, String> param : requestParam.entrySet()) {
                if(api.getRequestField(param.getKey()) != null && api.getRequestField(param.getKey()).getOperationType().needCollectionParam()){
                    requestParam.put(param.getKey(), String.join("\",\"", param.getValue().split(",")));
                } else {
                    requestParams.put(param.getKey(), param.getValue());
                }
            }
        }
        // fill default value
        for(ParamField field: api.getFieldMap().values()) {
            if(StringUtils.isNotBlank(field.getDefaultValue()) && !requestParams.containsKey(field.getName())){
                requestParams.put(field.getName(), field.getDefaultValue());
            }
        }
        requestParams.put("login_user_id", String.valueOf(LoginUtil.getInstance().getLoginUserId()));
        return requestParams;
    }

    public Object handleTempledExecute(Interface api, Map<String, String> requestParams){
        Object result = null;
        switch (api.getType()) {
            case SELECT:
                result = sqlMapper.sqlSelectList(api.getSqlFormatTemplate(requestParams), requestParams);;
                break;
            case INSERT:
                result = sqlMapper.sqlInsert(api.getSqlFormatTemplate(requestParams), requestParams);;
                break;
            case UPDATE:
                result = sqlMapper.sqlUpdate(api.getSqlFormatTemplate(requestParams), requestParams);
                break;
            case DELETE:
                result = sqlMapper.sqlDelete(api.getSqlFormatTemplate(requestParams), requestParams);
                break;
            default:
                break;
        }
        return result;
    }

    public BaseResponse handleDbExecute(Interface api,  Map<String, String[]> queryParam, Map<String, String> requestParam){
        Map<String, String> requestParams = prepareParams(api, queryParam, requestParam);

        Object result = null;
        if(StringUtils.isNotBlank(api.getSqlTemplate())){
            result = handleTempledExecute(api, requestParams);
        } else {
            switch (api.getType()) {
                case SELECT:
                    result = handleSelect(api, requestParams);
                    break;
                case INSERT:
                    result = handleInsert(api, requestParams);
                    break;
                case UPDATE:
                    result = handleUpdate(api, requestParams);
                    break;
                case DELETE:
                    result = handleDelete(api, requestParams);
                    break;
                default:
                    break;
            }
        }
        if(result == null){
            return BaseResponse.builder().code(ErrorCode.API_SQL_LIMIT_NOT_ENOUGH).build();
        }
        return BaseResponse.builder().data(result).build();
    }

    private static String getPathMapKey(String path, RequestMethod method){
        return method.name().toLowerCase() + path;
    }

    public BaseResponse handleRequest(String path, Map<String, String[]> queryParam, Map<String, String> requestParam, RequestMethod method){
        Interface api = interfaceMap.get(getPathMapKey(path, method));
        if(api == null){
            return BaseResponse.builder().code(ErrorCode.API_PATH_INVALID).build();
        }
        // validate request method
        if(!method.equals(api.getRequestMethod())){
            return BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
        }
        // validate entity count
//        if(!method.equals(api.getRequestMethod())){
//            BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
//        }
        // upsert接口的实体个数不能超过一个
        if(DbType.INSERT.equals(api.getType()) && (api.getEntityTableMap().keySet().size() > 1)){
            return BaseResponse.builder().code(ErrorCode.API_UPSERT_DUPLICATE_ENTITY).build();
        }
        // validate request param
        BaseResponse resp = validateRequestParam(api.getQuery(), queryParam);
        if(!resp.isSuceeded()){
            return resp;
        }
        resp = validateRequestParam(api.getParam(), requestParam);
        if(!resp.isSuceeded()){
            return resp;
        }
        // construct request param to db execute
        return handleDbExecute(api, queryParam, requestParam);
    }

    public BaseResponse post(String path, Map<String, String[]> query, Map<String, String> param){
        return handleRequest(path, null, param, RequestMethod.POST);
    }

    public BaseResponse put(String path, Map<String, String[]> query, Map<String, String> param){
        return handleRequest(path, query, param, RequestMethod.PUT);
    }

    public BaseResponse get(String path, Map<String, String[]> query){
        return handleRequest(path, query, null, RequestMethod.GET);
    }
    public BaseResponse delete(String path, Map<String, String[]> query){
        return handleRequest(path, query, null, RequestMethod.DELETE);
    }


}
