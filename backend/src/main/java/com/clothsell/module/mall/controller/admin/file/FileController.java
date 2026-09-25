package com.clothsell.module.mall.controller.admin.file;

import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.framework.common.pojo.CommonResult;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_EMPTY;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_UPLOAD;

@RestController
@RequestMapping("/mall/file")
public class FileController {
    @Value("${app.upload-dir}")
    private String uploadDir;
    @Value("${app.qiniu.access-key:}")
    private String accessKey;
    @Value("${app.qiniu.secret-key:}")
    private String secretKey;
    @Value("${app.qiniu.bucket:}")
    private String bucket;
    @Value("${app.qiniu.domain:}")
    private String domain;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('mall:product:create') or @ss.hasPermission('mall:product:update')")
    public CommonResult<String> create(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw exception(FILE_EMPTY);
        }
        String ext = extension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        if (accessKey == null || accessKey.isBlank()) {
            return success(saveLocal(file, filename));
        }
        return success(saveQiniu(file, filename));
    }

    private String saveLocal(MultipartFile file, String filename) throws IOException {
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));
        return "/files/" + filename;
    }

    private String saveQiniu(MultipartFile file, String filename) throws IOException {
        if (secretKey.isBlank() || bucket.isBlank() || domain.isBlank()) {
            throw exception(FILE_UPLOAD);
        }
        String key = "covers/" + filename;
        Auth auth = Auth.create(accessKey, secretKey);
        UploadManager uploadManager = new UploadManager(new Configuration(Region.xinjiapo()));
        try {
            Response response = uploadManager.put(file.getBytes(), key, auth.uploadToken(bucket));
            if (!response.isOK()) {
                throw new ServiceException(FILE_UPLOAD.getCode(), "图片上传失败：" + response.error);
            }
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(FILE_UPLOAD.getCode(), "图片上传失败：" + ex.getMessage());
        }
        String base = domain.startsWith("http") ? domain : "https://" + domain;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + key;
    }

    private String extension(String originalName) {
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return ".png";
        }
        if (name.endsWith(".gif")) {
            return ".gif";
        }
        if (name.endsWith(".webp")) {
            return ".webp";
        }
        return ".jpg";
    }
}
