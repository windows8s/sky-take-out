package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public Result<String> fileUpload(MultipartFile file) {
        //检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("这个文件是空的，请检查你的前端是不是传进来了老毕登");
        }

        //创建目标文件夹
        String uploadDirectory = "D:\\images\\";
        File directory = new File(uploadDirectory);
        //判断文件夹是否存在
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                return Result.error("无法创建上传目录");
            }
        }

        //获取前端上传的文件名
        String originalFilename = file.getOriginalFilename();
        //生成唯一的文件名以避免潜在的重名问题
        String uniqueFilename = System.currentTimeMillis()+ "_" +originalFilename  ;
        //将文件存储在服务器的磁盘目录中
        File destFile = new File(uploadDirectory + uniqueFilename);
        String destFilePath = destFile.getAbsolutePath();
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件存储失败");
        }
        return Result.success(destFilePath);
    }
}
