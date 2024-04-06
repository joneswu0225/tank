package com.jones.tank.object;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

//@Configuration
public class ApplicationConst {
    public static String APP_DOMAIN;
    public static Integer DEPLOY_ID;
    public static List<String> APP_TABLE_NAMES;
//    @Value("${app.deployId}")
    public void setDeployId(Integer deployId) {
        DEPLOY_ID = deployId;
    }
//    @Value("${app.domain:vr2shipping.com}")
    public void setAppDomain(String appDomain) {
        APP_DOMAIN = appDomain;
    }

//    @Value("${app.tables:message,room,room_user,room_user_message}")
    public void setAppTableNames(String appTableNames) {
        APP_TABLE_NAMES = Arrays.asList(appTableNames.split(","));
    }
    public static Integer getTableId(String tableName){
        return APP_TABLE_NAMES.indexOf(tableName) + 1;
    }

}
