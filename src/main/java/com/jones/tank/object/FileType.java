package com.jones.tank.object;

import java.io.File;

public enum FileType {
    USER_NAMECARD("用户名片"),
    USER_AVATAR("用户头像"),
    USER_GALLERY("用户相册"),
    ENTERPRIISE_NAMECARD("企业名片"),
    ENTERPRIISE_AVATAR("企业头像"),
    ENTERPRIISE_GALLERY("企业相册")
    ;

    public final String description;

    FileType(String description) {
        this.description = description;
    }

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
            case USER_GALLERY:
                relPath = "user" + File.separator + "gallery" + File.separator + name;
                break;
            case ENTERPRIISE_NAMECARD:
                relPath = "enterprise" + File.separator + "namecard" + File.separator + name;
                break;
            case ENTERPRIISE_AVATAR:
                relPath = "enterprise" + File.separator + "avatar" + File.separator + name;
                break;
            case ENTERPRIISE_GALLERY:
                relPath = "enterprise" + File.separator + "gallery" + File.separator + name;
                break;
            default:
                relPath = "";
        }
        return ApplicationConst.FILE_PATH_PREFIX + relPath;
    }
}

