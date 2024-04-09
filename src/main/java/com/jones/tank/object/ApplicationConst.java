package com.jones.tank.object;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

//@Configuration
public class ApplicationConst {
    public static final int PLATEFROM = 1;
    public static final int NOPLATEFROM = 0;
    public static final String APP_SOURCE_FIELD = "App-Source";
    public static final String APP_SOURCE_ADMIN = "ADMIN";
    public static final String APP_SOURCE_H5 = "H5";
    public static final String APP_SOURCE_PC = "PC";
    public static final String APP_SOURCE_WEIXIN = "WEIXIN";
    public static final String APP_MODE_DEBUG = "DEBUG";
    public static final String APP_MODE_NOLOGIN = "NOLOGIN";
    public static final String APP_MODE_PRODUCT = "PRODUCT";

    public static String APP_DOMAIN;
    public static String DEPLOY_ID;
    @Value("${app.domain:vr2shipping.com}")
    public void setAppDomain(String appDomain) {
        APP_DOMAIN = appDomain;
    }


}
