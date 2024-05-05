package com.jones.tank.object;

import java.io.File;

public enum FileType {
    USER_NAMECARD("用户名片"),
    USER_AVATAR("用户头像"),
    USER_GALLERY("用户相册"),
    USER_RESUME("个人简历"),
    ENTERPRISE_NAMECARD("企业名片"),
    ENTERPRISE_AVATAR("企业头像"),
    ENTERPRISE_GALLERY("企业相册"),
    ACTION_IMAGE("活动图片"),
    SHARE_FILE("分享文件")
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
            case USER_RESUME:
                relPath = "user" + File.separator + "resume" + File.separator + name;
                break;
            case ENTERPRISE_NAMECARD:
                relPath = "enterprise" + File.separator + "namecard" + File.separator + name;
                break;
            case ENTERPRISE_AVATAR:
                relPath = "enterprise" + File.separator + "avatar" + File.separator + name;
                break;
            case ENTERPRISE_GALLERY:
                relPath = "enterprise" + File.separator + "gallery" + File.separator + name;
                break;
            case ACTION_IMAGE:
                relPath = "action" + File.separator + "image" + File.separator + name;
                break;
            case SHARE_FILE:
                relPath = "share" + File.separator + "file" + File.separator + name;
                break;
            default:
                relPath = "";
        }
        return ApplicationConst.FILE_PATH_PREFIX + relPath;
    }
}

