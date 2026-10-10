package com.clothsell.module.mall.service.file;

import java.io.IOException;

public interface FileService {
    String upload(byte[] content) throws IOException;
}
