package com.jones.tank.object;

import java.io.File;

public enum FileType {
    USER_NAMECARD("用户名片"),
    USER_AVATAR("用户头像"),
    ENTERPRIISE_IMAGE("企业图标"),
    ENTERPRISE_LOGO("企业LOGO")
    ;

    public final String description;

    FileType(String description) {
        this.description = description;
    }

    public static final String FILE_PATH_PREFIX = "static/";
    public String getFilePath(String relatedId, String fileName){
        String name = this.name() + "_" + relatedId + "_" + System.currentTimeMillis() + "_" + fileName;
        String relPath = "";
        switch (this){
            case USER_NAMECARD:
                relPath = "user" + File.separator + "namecard" + File.separator + name;
                break;
            case USER_AVATAR:
                relPath = "user" + File.separator + "avatar" + File.separator + name;
                break;
            case ENTERPRIISE_IMAGE:
            case ENTERPRISE_LOGO:
                relPath = "enterprise" + File.separator + relatedId + File.separator + "image" + File.separator + name;
                break;
            default:
                relPath = "";
        }
        return FILE_PATH_PREFIX + relPath;
    }
}

