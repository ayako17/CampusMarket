package com.backend.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder; // 新增
import java.nio.charset.StandardCharsets; // 新增
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }
        try {
            // 生成唯一文件名
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + fileName);
            // 确保目录存在
            dest.getParentFile().mkdirs();
            // 保存文件
            file.transferTo(dest.getAbsoluteFile());
            // 返回文件访问路径
            String fileUrl = "http://localhost:6005/api/files/" + fileName;
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(500).body("文件上传失败");
        }
    }
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws IOException {
        // 使用相对路径，确保在不同操作系统上都能正确找到文件
        File file = new File(UPLOAD_DIR + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        // 动态获取 MIME 类型
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认类型
        }
        String originalFilename = file.getName();
        // 1. 使用 URLEncoder 对文件名进行 UTF-8 编码
        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20"); // 替换空格的编码
        // 2. 使用 filename* 参数来支持 UTF-8 编码 (RFC 6266 推荐)
        // 同时保留 filename 参数以兼容旧版浏览器
        String contentDisposition = String.format("inline; filename=\"%s\"; filename*=utf-8''%s",
                originalFilename, encodedFilename);
          return ResponseEntity.ok()
                // 设置 Content-Disposition 头，支持中文文件名
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }
}