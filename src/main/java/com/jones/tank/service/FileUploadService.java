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
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

    private FileUpload generateFileUpload(String fileName, String fileOriginName, FileType fileType, String relatedId){
        fileName = StringUtils.isEmpty(fileName) ? fileOriginName : fileName;
        int fileSurfixIndex = fileName.lastIndexOf(".");
        int originFileSurfixIndex = fileOriginName == null ? -1 : fileOriginName.lastIndexOf(".") ;
        String namePart = fileSurfixIndex > 0 ? fileName.substring(0, fileSurfixIndex) : fileName;
        String fileSurfix = originFileSurfixIndex > 0 ? fileOriginName.substring(originFileSurfixIndex + 1) : (fileSurfixIndex > 1 ? fileName.substring(fileSurfixIndex + 1) : null);
        fileName = StringUtils.isEmpty(fileSurfix) ? namePart : (namePart + "." + fileSurfix);

        String relPath = fileType.getFilePath(relatedId, fileName);

        FileUpload fileUpload = FileUpload.builder().type(fileType).path(relPath).name(fileName).domain(ApplicationConst.APP_DOMAIN).relatedId(relatedId).build();
        if(LoginUtil.getInstance().getUser() != null){
            fileUpload.setUserId(LoginUtil.getInstance().getLoginUserId());
        }
        return fileUpload;

    }
    public BaseResponse uploadFile(MultipartFile file, String fileName, FileType fileType, String relatedId){
        try {
            FileUpload fileUpload = generateFileUpload(fileName, file.getOriginalFilename(), fileType, relatedId);
            String relPath = fileUpload.getPath();
            Path path = Paths.get(ApplicationConst.UPLOAD_PATH, relPath);
            path.toFile().mkdirs();
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            mapper.insert(fileUpload);
            Map<String, String> result = new HashMap<>();
            result.put("path", relPath);
            return BaseResponse.builder().data(result).build();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return BaseResponse.builder().code(ErrorCode.UPLOAD_FAILED).data(e.getMessage()).build();
        }
    }

    public BaseResponse uploadFile(String base64, String fileName, FileType fileType, String relatedId){
        FileUpload fileUpload = generateFileUpload(fileName, null, fileType, relatedId);
        String relPath = fileUpload.getPath();
        Path path = Paths.get(ApplicationConst.UPLOAD_PATH, relPath);
        new File(path.toFile().getParent()).mkdirs();
        int contentIndex = base64.indexOf(",");
        base64 = contentIndex > 0 ? base64.substring(contentIndex+1) : base64;
        try {
            Files.write(path, Base64.getDecoder().decode(base64), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapper.insert(fileUpload);
        Map<String, String> result = new HashMap<>();
        result.put("path", relPath);
        return BaseResponse.builder().data(result).build();
    }

        public String getFileUploadPath(){
        return fileUploadPath;
    }


}

