package com.jones.tank.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Intent {
    private Integer id;
    private String title;
    private String type;
    private String content;
    private Integer isHuafa;

}

