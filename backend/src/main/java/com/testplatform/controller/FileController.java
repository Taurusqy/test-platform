package com.testplatform.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传 Controller
 * 支持上传截图等附件，返回可访问的URL
 */
@RestController
@RequestMapping("/api/upload")
public class FileController {

    // 上传文件保存目录（项目根目录下的 uploads 文件夹）
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建上传目录
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 生成唯一文件名：UUID_原始文件名，既避免重名又保留中文名
            String originalFilename = file.getOriginalFilename();
            String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            // 保存文件
            File dest = new File(UPLOAD_DIR + newFilename);
            file.transferTo(dest);
            // 返回可访问的URL（通过静态资源映射访问）
            result.put("code", 200);
            result.put("url", "/uploads/" + newFilename);
            result.put("filename", originalFilename);
        } catch (IOException e) {
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }
}
