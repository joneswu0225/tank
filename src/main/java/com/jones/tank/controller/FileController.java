package com.jones.tank.controller;

import com.jones.tank.object.BaseResponse;
import com.jones.tank.entity.query.Query;
import com.jones.tank.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/file")
@Slf4j
@Api(value = "上传下载", tags = {"上传下载"})
public class FileController{
    @Autowired
    private FileUploadService service;

    @PostMapping(value="/upload")
    public BaseResponse fileUpload(
            @RequestParam(name="file") @ApiParam(value="上传文件",name="file") MultipartFile file,
            @RequestParam(name="fileName", required = false) @ApiParam(value="文件名称",name="fileName") String fileName) throws Exception{
        BaseResponse resp = service.uploadFile(file, fileName);
        return resp;
    }

    @GetMapping("/list")
    public BaseResponse fileDownLoad() throws Exception{
        return BaseResponse.builder().data(service.findList(new Query())).build();
    }

    @GetMapping("/download")
    public void fileDownLoad(@RequestParam("path") String path, HttpServletResponse response) throws Exception{
//        String fileName = path.split("_")[1];
//        fileName=new String(fileName.getBytes("gbk"),"iso8859-1");//防止中文乱码
        Path realPath = Paths.get(service.getFileUploadPath(), path).toAbsolutePath();
//        HttpHeaders headers=new HttpHeaders();//设置响应头
//        headers.add("Content-Disposition", "attachment;filename="+realPath.getFileName().toString());
        HttpStatus statusCode = HttpStatus.OK;//设置响应吗
            response.addHeader("Content-Disposition", "attachment;filename="+realPath.getFileName().toString());
        IOUtils.copy(new FileInputStream(realPath.toFile()), response.getOutputStream());
//        ResponseEntity<byte[]> response=new ResponseEntity(IOUtils..toByteArray(realPath.toUri()), headers, statusCode);
//        return response;
    }

}
