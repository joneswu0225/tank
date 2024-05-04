package com.jones.tank.entity.param;

import com.jones.tank.object.FileType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="文件上传参数")
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadParam {

    @NotBlank(message = "图片名称")
    @ApiModelProperty(value="文件名称",name="fileName")
    private String fileName;
    @NotBlank(message = "上传文件base64")
    @ApiModelProperty(value="上传文件base64",name="fileBase64")
    private String fileBase64;
    @ApiModelProperty(value="关联内容的id， 如上传名片为user_id, 上传企业avatar为enterpirse_id",name="relatedId")
    @NotBlank(message = "关联内容的id， 如上传名片为user_id, 上传企业avatar为enterpirse_id")
    private String relatedId;
    @ApiModelProperty(value="文件类型",name="fileType")
    private FileType fileType;
}

