package com.jones.tank.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUpload {
    private Integer id;
    private String path;
    private String name;
    private String type;
    private String create_time;
}

