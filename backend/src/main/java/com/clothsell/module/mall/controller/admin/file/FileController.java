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
import java.util.UUID;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_EMPTY;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_TYPE;
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
        byte[] bytes = file.getBytes();
        String ext = sniff(bytes);
        if (ext == null) {
            throw exception(FILE_TYPE);
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        if (accessKey == null || accessKey.isBlank()) {
            return success(saveLocal(bytes, filename));
        }
        return success(saveQiniu(bytes, filename));
    }

    private String saveLocal(byte[] bytes, String filename) throws IOException {
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), bytes);
        return "/files/" + filename;
    }

    private String saveQiniu(byte[] bytes, String filename) throws IOException {
        if (secretKey.isBlank() || bucket.isBlank() || domain.isBlank()) {
            throw exception(FILE_UPLOAD);
        }
        String key = "covers/" + filename;
        Auth auth = Auth.create(accessKey, secretKey);
        UploadManager uploadManager = new UploadManager(new Configuration(Region.xinjiapo()));
        try {
            Response response = uploadManager.put(bytes, key, auth.uploadToken(bucket));
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

    private String sniff(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xd8 && (data[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if (data.length >= 8 && data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return ".png";
        }
        if (data.length >= 6 && data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8') {
            return ".gif";
        }
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return ".webp";
        }
        return null;
    }
}
