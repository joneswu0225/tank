package com.jones.tank.entity;

import com.jones.tank.object.FileType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUpload {
    private Integer id;
    private String path;
    private String name;
    private String relatedId;
    private String domain;
    private Long userId;
    private FileType type;
    private String create_time;
}

