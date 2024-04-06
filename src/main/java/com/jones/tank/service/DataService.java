package com.jones.tank.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jones.tank.entity.*;
import com.jones.tank.entity.query.*;
import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.ErrorCode;
import com.jones.tank.object.dataapi.DbType;
import com.jones.tank.object.dataapi.TableType;
import com.jones.tank.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.jones.tank.object.dataapi.DbType.*;

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

    private void handleTableInfo(){


//        Map<String, List<TableInfo>>
    }
    @PostConstruct
    private void refresh(){
        Map<String, TableInfo> tableInfoMap = new HashMap<>();

        List<Interface> interfaceList = interfaceMapper.findAll(new InterfaceQuery());
        List<AttributeInfo> attributeInfoList = attributeInfoMapper.findAll(new AttributeInfoQuery());
        List<Param> paramList = paramMapper.findAll(new ParamQuery());
        Map<Long, AttributeInfo> attributeInfoMap = new HashMap<>();
        List<TableInfo> tableInfoList = tableInfoMapper.findAll(new TableInfoQuery());
        Map<Long, TableInfo> tableInfoIdMap = new HashMap<>();
        for(TableInfo table: tableInfoList){
            tableInfoIdMap.put(table.getId(), table);
            tableInfoMap.put(table.getName(), table);
            if (StringUtils.isEmpty(table.getKey1())) {
                continue;
            } else {
                table.getKeys().add("id".equals(table.getKey1()) ? table.getName() + "_id" : table.getKey1());
            }
            if (StringUtils.isEmpty(table.getKey2())) {
                continue;
            } else {
                table.getKeys().add(table.getKey2());
            }
            if (!StringUtils.isEmpty(table.getKey3())) {
                table.getKeys().add(table.getKey3());
            }
        }
        for(AttributeInfo attributeInfo: attributeInfoList){
            attributeInfo.setTableInfo(tableInfoIdMap.get(attributeInfo.getTableId()));
            attributeInfoMap.put(attributeInfo.getId(), attributeInfo);
        }
        paramList.forEach(p->{
            p.getFields().forEach(k->{k.setAttribute(attributeInfoMap.get(k.getAttributeId()));});
            p.setFieldMap(p.getFields().stream().collect(Collectors.toMap(k->k.getName(), k->k)));
        });
        Map<Long, Param> paramMap = new HashMap<>();
        for(Param param: paramList){
            param.setRequiredFields(param.getFields().stream().filter(p->ParamField.REQUIRED.equals(p.getRequired())).map(ParamField::getName).collect(Collectors.toList()));
            param.setFieldMap(param.getFields().stream().collect(Collectors.toMap(k->k.getName(), k->k)));
//            param.getTableNames().addAll(param.getFields().stream().map(p->p.getAttribute().getTableInfo().getName()).collect(Collectors.toList()))
            paramMap.put(param.getId(), param);
        }
        ConcurrentHashMap<String, Interface> pathMap = new ConcurrentHashMap<>();
        for(Interface item: interfaceList){
            item.setParam(paramMap.get(item.getParamId()));
            item.setResult(paramMap.get(item.getResultId()));
            for(ParamField field: item.getParam().getFields()) {
                item.getTableParams().putIfAbsent(field.getAttribute().getTableInfo(), new HashSet<>());
                item.getTableParams().get(field.getAttribute().getTableInfo()).add(field.getAttribute());
                item.getParamFieldMap().put(field.getName(), field);
            }
            for(ParamField field: item.getResult().getFields()){
                item.getTableParams().putIfAbsent(field.getAttribute().getTableInfo(), new HashSet<>());
                item.getTableParams().get(field.getAttribute().getTableInfo()).add(field.getAttribute());
            }
            item.generateJoinInfo(tableInfoMap);
            pathMap.put(item.getPath(), item);
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

    public List<Map<String, Object>> handleSelect(Interface api, Map<String, String[]> requestParam){
        // format request params
        Map<String, String> requestParams = new HashMap<>();
        for(ParamField field: api.getParam().getFields()) {
            if(!StringUtils.isEmpty(field.getDefaultValue())){
                requestParams.put(field.getName(), field.getDefaultValue());
            }
        }
        for(Map.Entry<String, String[]> param: requestParam.entrySet()){
            requestParams.put(param.getKey(),
                    api.getParamFieldMap().get(param.getKey()).getOperationType().needCollectionParam() ?
                            String.join("','",  param.getValue()) : param.getValue()[0]
            );
        }
        // TODO order by        .append(" ORDER BY ")
        String query = api.getSelectTemplate(requestParams);
        System.out.printf(query);
        return sqlMapper.sqlSelectList(query, requestParams);
    }

    public void handleUpsert(Interface api, Map<String, String[]> requestParam){

    }
    public BaseResponse handleDbExecute(Interface api,  Map<String, String[]> requestParam){
        switch (api.getType()){
            case SELECT:
                List data = handleSelect(api, requestParam);
                return BaseResponse.builder().data(data).build();
            default:
                handleUpsert(api, requestParam);
                break;
        }
        return BaseResponse.builder().build();
    }
    public BaseResponse handleRequest(String path, Map<String, String[]> requestParam, RequestMethod method){
        path = path.substring(REQUEST_PREFIX.length());
        // validate request path
        if(!interfaceMap.containsKey(path)){
            BaseResponse.builder().code(ErrorCode.API_PATH_INVALID).build();
        }
        // validate request method
        Interface api = interfaceMap.get(path);
        if(!method.equals(api.getRequestMethod())){
            BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
        }
        // validate entity count
        if(!method.equals(api.getRequestMethod())){
            BaseResponse.builder().code(ErrorCode.API_METHOD_INVALID).build();
        }
        // validate request param
        BaseResponse resp = validateRequestParam(api.getParam(), requestParam);
        if(!resp.isSuceeded()){
            return resp;
        }
        // construct request param to db execute
        return handleDbExecute(api, requestParam);
    }

    public BaseResponse insert(String path, Map<String, String[]> param){
        return handleRequest(path, param, RequestMethod.POST);
    }

    public BaseResponse select(String path, Map<String, String[]> param){
        return handleRequest(path, param, RequestMethod.GET);
    }


}
