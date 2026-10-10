package com.clothsell.module.mall.service.file.impl;

import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.module.mall.service.file.FileService;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_EMPTY;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_TYPE;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.FILE_UPLOAD;

@Service
public class FileServiceImpl implements FileService {
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

    @Override
    public String upload(byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw exception(FILE_EMPTY);
        }
        String extension = sniff(content);
        if (extension == null) {
            throw exception(FILE_TYPE);
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        return accessKey == null || accessKey.isBlank()
                ? saveLocal(content, filename)
                : saveQiniu(content, filename);
    }

    private String saveLocal(byte[] content, String filename) throws IOException {
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), content);
        return "/files/" + filename;
    }

    private String saveQiniu(byte[] content, String filename) throws IOException {
        if (secretKey.isBlank() || bucket.isBlank() || domain.isBlank()) {
            throw exception(FILE_UPLOAD);
        }
        String key = "covers/" + filename;
        Auth auth = Auth.create(accessKey, secretKey);
        UploadManager uploadManager = new UploadManager(new Configuration(Region.xinjiapo()));
        try {
            Response response = uploadManager.put(content, key, auth.uploadToken(bucket));
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
