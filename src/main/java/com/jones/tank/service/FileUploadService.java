package com.jones.tank.service;

import com.jones.tank.entity.FileUpload;
import com.jones.tank.object.*;
import com.jones.tank.repository.FileUploadMapper;
import com.jones.tank.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FileUploadService extends CustomServiceImpl<FileUploadMapper, FileUpload> {
    public static String fileUploadPath;
    @Value("${app.file.path.upload:./static/files}")
    public void setFileUploadPath(String uploadPath){
        FileUploadService.fileUploadPath = uploadPath;
    }

    @PostConstruct
    private void init(){
        File path = Paths.get(fileUploadPath).toFile();
        if(!path.exists()){
            path.mkdirs();
        }
    }

    @Autowired
    private FileUploadMapper mapper;

    public BaseResponse uploadFile(MultipartFile file, String fileName, FileType fileType, String relatedId){
        try {
            fileName = StringUtils.isEmpty(fileName) ? file.getOriginalFilename() : fileName;
            int fileSurfixIndex = fileName.lastIndexOf(".");
            int originFileSurfixIndex = file.getOriginalFilename().lastIndexOf(".");
            fileName = fileSurfixIndex > 0 ? fileName.substring(0, fileSurfixIndex) : fileName;
            String fileSurfix = originFileSurfixIndex > 0 ? file.getOriginalFilename().substring(originFileSurfixIndex + 1) : (fileSurfixIndex > 1 ? fileName.substring(fileSurfixIndex + 1) : null);
            fileName = StringUtils.isEmpty(fileSurfix) ? fileName : (fileName + "." + fileSurfix);
            String relPath = fileType.getFilePath(relatedId, fileName);
            Path path = Paths.get(ApplicationConst.UPLOAD_PATH, relPath);
            path.toFile().mkdirs();
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            FileUpload fileUpload = FileUpload.builder().type(fileType).path(relPath).name(fileName).domain(ApplicationConst.APP_DOMAIN).relatedId(relatedId).build();
            if(LoginUtil.getInstance().getUser() != null){
                fileUpload.setUserId(LoginUtil.getInstance().getLoginUserId());
            }
            mapper.insert(fileUpload);
            Map<String, String> result = new HashMap<>();
            result.put("path", relPath);
            return BaseResponse.builder().data(result).build();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return BaseResponse.builder().code(ErrorCode.UPLOAD_FAILED).data(e.getMessage()).build();
        }
    }

    public String getFileUploadPath(){
        return fileUploadPath;
    }


}

