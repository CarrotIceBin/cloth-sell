package com.clothsell.module.mall.controller.admin.file;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.service.file.FileService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/file")
public class FileController {
    @Resource
    private FileService fileService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('mall:product:create') or @ss.hasPermission('mall:product:update')")
    public CommonResult<String> create(@RequestParam("file") MultipartFile file) throws IOException {
        return success(fileService.upload(file.getBytes()));
    }
}
