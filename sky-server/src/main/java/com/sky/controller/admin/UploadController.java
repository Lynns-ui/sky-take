package com.sky.controller.admin;

import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/admin/common/upload")
@RestController
@Slf4j
public class UploadController {

    @Autowired
    private AliOssProperties aliOssProperties;

    @PostMapping
    public Result<String> uploadFile(MultipartFile file) throws IOException {
        log.info("上传文件:{}",file);

        AliOssUtil aliOssUtil = new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());

        String upload = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
        return Result.success(upload);
    }
}
